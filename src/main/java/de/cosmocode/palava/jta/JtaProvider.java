/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cosmocode.palava.jta;

import javax.naming.*;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import com.google.inject.name.Named;
import de.cosmocode.palava.core.lifecycle.Disposable;
import org.jboss.util.naming.NonSerializableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;

import java.io.File;
import java.util.Properties;

/**
 * Binds the Bitronix JTA provider as the JTA manager.
 * 
 * @author Tobias Sarnowski
 */
final class JtaProvider implements Initializable, Disposable {
    
    private static final Logger LOG = LoggerFactory.getLogger(JtaProvider.class);

    private final Provider<Context> provider;
    private File storage;

    private String manager = JtaConfig.DEFAULT_MANAGER;
    private String user = JtaConfig.DEFAULT_USER;

    private BitronixTransactionManager btm;

    @Inject
    public JtaProvider(Provider<Context> provider, @Named(JtaConfig.STORAGE_DIRECTORY) File storage) {
        this.provider = provider;
        this.storage = storage;
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
            LOG.trace("Configuring JTA provider");
            TransactionManagerServices.getConfiguration().setLogPart1Filename(new File(storage, "log1").getAbsolutePath());
            TransactionManagerServices.getConfiguration().setLogPart2Filename(new File(storage, "log2").getAbsolutePath());

            btm = TransactionManagerServices.getTransactionManager();
            LOG.debug("Starting JTA provider");
            btm.begin();

            final TransactionManager tm = btm;
            final UserTransaction tx = btm;

            LOG.info("Binding TransactionManager to {} [{}]", manager, tm);
            //bind(manager, tm, tm.getClass(), context);
            bind(context, manager, tm);

            LOG.info("Binding UserTransaction to {} [{}]", user, tx);
            //bind(user, tx, tx.getClass(), context);
            bind(context, user, tx);
        /* CHECKSTYLE:OFF */
        } catch (Exception e) {
        /* CHECKSTYLE:ON */
            throw new LifecycleException(e);
        }
    }

    @Override
    public void dispose() throws LifecycleException {
        btm.shutdown();
    }

    /**
     * Helper method that binds a non serializable object to the JNDI tree.
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

    private void bind(Context context, String jndiName, Object obj) throws NamingException {
        Context ctx = context;
        Name name = ctx.getNameParser("").parse(jndiName);
        while (name.size() > 1) {
            final String ctxName = name.get(0);
            try {
                ctx = (Context)ctx.lookup(ctxName);
                LOG.trace("Subcontext {} already exists", ctxName);
            }catch (NameNotFoundException e) {
                LOG.info("Creating Subcontext {}", ctxName);
                ctx = ctx.createSubcontext(ctxName);
            }
            name = name.getSuffix(1);
        }
        ctx.bind(name, obj);
        //context.bind(jndiName, obj);
    }
}