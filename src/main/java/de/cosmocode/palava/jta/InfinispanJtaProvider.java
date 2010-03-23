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

import com.google.inject.Inject;
import com.google.inject.Provider;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import org.infinispan.transaction.lookup.JBossStandaloneJTAManagerLookup;
import org.jboss.util.naming.NonSerializableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.*;

/**
 * @author Tobias Sarnowski
 */
class InfinispanJtaProvider implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(InfinispanJtaProvider.class);

    private static final JBossStandaloneJTAManagerLookup lookup = new JBossStandaloneJTAManagerLookup();

    private Provider<Context> contextProvider;

    @Inject
    public InfinispanJtaProvider(Provider<Context> contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public void initialize() throws LifecycleException {
        Context ctx = contextProvider.get();
        try {
            bind("java:/TransactionManager", lookup.getTransactionManager(), lookup.getTransactionManager().getClass(), ctx);
            bind("UserTransaction", lookup.getUserTransaction(), lookup.getUserTransaction().getClass(), ctx);
        } catch (Exception e) {
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
    private void bind(String jndiName, Object who, Class classType, Context ctx) throws Exception {
        // Ah ! This service isn't serializable, so we use a helper class
        NonSerializableFactory.bind(jndiName, who);
        Name n = ctx.getNameParser("").parse(jndiName);
        while (n.size() > 1) {
            String ctxName = n.get(0);
            try {
                ctx = (Context) ctx.lookup(ctxName);
            } catch (NameNotFoundException e) {
                LOG.info("Creating subcontext: {}", ctxName);
                ctx = ctx.createSubcontext(ctxName);
            }
            n = n.getSuffix(1);
        }

        // The helper class NonSerializableFactory uses address type nns, we go on to
        // use the helper class to bind the service object in JNDI
        StringRefAddr addr = new StringRefAddr("nns", jndiName);
        Reference ref = new Reference(classType.getName(), addr, NonSerializableFactory.class.getName(), null);
        ctx.rebind(n.get(0), ref);
    }
}
