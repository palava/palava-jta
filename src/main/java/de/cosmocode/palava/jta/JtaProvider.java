/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package de.cosmocode.palava.jta;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import com.google.inject.name.Named;
import org.jboss.util.naming.NonSerializableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;

/**
 * Binds the JBoss default provider as the JTA manager.
 * 
 * @author Tobias Sarnowski
 */
final class JtaProvider implements Initializable {
    
    private static final Logger LOG = LoggerFactory.getLogger(JtaProvider.class);

    private final Provider<Context> provider;

    private String manager = JtaConfig.DEFAULT_MANAGER;
    private String user = JtaConfig.DEFAULT_USER;

    @Inject
    public JtaProvider(Provider<Context> provider) {
        this.provider = provider;
    }

    @Inject(optional = true)
    public void setManager(@Named(JtaConfig.MANAGER) String manager) {
        this.manager = manager;
    }

    @Inject(optional = true)
    public void setUser(@Named(JtaConfig.USER) String user) {
        this.user = user;
    }

    @Override
    public void initialize() throws LifecycleException {
        final Context context = provider.get();

        try {
            final TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
            final UserTransaction tx = com.arjuna.ats.jta.UserTransaction.userTransaction();
            
            bind(manager, tm, tm.getClass(), context);
            bind(user, tx, tx.getClass(), context);
        /* CHECKSTYLE:OFF */
        } catch (Exception e) {
        /* CHECKSTYLE:ON */
            throw new LifecycleException(e);
        }
    }

    /**
     * Helper method that binds the a non serializable object to the JNDI tree.
     *
     * @param jndiName  Name under which the object must be bound
     * @param who       Object to bind in JNDI
     * @param classType Class type under which should appear the bound object
     * @param ctx       Naming context under which we bind the object
     * @throws Exception Thrown if a naming exception occurs during binding
     */
    private void bind(String jndiName, Object who, Class<?> classType, Context ctx) throws Exception {
        // Ah ! This service isn't serializable, so we use a helper class
        Context context = ctx;
        NonSerializableFactory.bind(jndiName, who);
        Name n = ctx.getNameParser("").parse(jndiName);
        while (n.size() > 1) {
            final String ctxName = n.get(0);
            try {
                context = (Context) context.lookup(ctxName);
            } catch (NameNotFoundException e) {
                LOG.info("Creating subcontext: {}", ctxName);
                context = context.createSubcontext(ctxName);
            }
            n = n.getSuffix(1);
        }

        // The helper class NonSerializableFactory uses address type nns, we go on to
        // use the helper class to bind the service object in JNDI
        final StringRefAddr addr = new StringRefAddr("nns", jndiName);
        final Reference ref = new Reference(classType.getName(), addr, NonSerializableFactory.class.getName(), null);
        ctx.rebind(n.get(0), ref);
    }
}
