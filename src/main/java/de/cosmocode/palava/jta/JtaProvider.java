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

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import de.cosmocode.palava.jmx.MBeanService;
import de.cosmocode.palava.jndi.JNDIContextBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.io.File;

/**
 * Binds the Bitronix JTA provider as the JTA manager.
 * 
 * @author Tobias Sarnowski
 */
final class JtaProvider implements Initializable, Disposable {
    
    private static final Logger LOG = LoggerFactory.getLogger(JtaProvider.class);

    private File storage;
    private JNDIContextBinder jndiContextBinder;
    private MBeanService mBeanService;

    private String manager = JtaConfig.DEFAULT_MANAGER;
    private String user = JtaConfig.DEFAULT_USER;

    private BitronixTransactionManager btm;
    private final TransactionCounter counter = new TransactionCounter();

    @Inject
    public JtaProvider(@Named(JtaConfig.STORAGE_DIRECTORY) File storage,
                       JNDIContextBinder jndiContextBinder,
                       MBeanService mBeanService) {
        this.storage = storage;
        this.jndiContextBinder = jndiContextBinder;
        this.mBeanService = mBeanService;
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
        try {
            LOG.trace("Configuring JTA provider");
            TransactionManagerServices.getConfiguration().setLogPart1Filename(new File(storage, "log1").getAbsolutePath());
            TransactionManagerServices.getConfiguration().setLogPart2Filename(new File(storage, "log2").getAbsolutePath());

            btm = TransactionManagerServices.getTransactionManager();
            LOG.debug("Starting JTA provider");
            btm.begin();

            final TransactionManager tm = new TransactionManagerCounter(btm, counter);
            final UserTransaction tx = new UserTransactionCounter(btm, counter);

            LOG.info("Binding TransactionManager to {} [{}]", manager, tm);
            jndiContextBinder.bind(manager, tm, TransactionManager.class);

            LOG.info("Binding UserTransaction to {} [{}]", user, tx);
            jndiContextBinder.bind(user, tx, UserTransaction.class);

            mBeanService.register(counter);

        /* CHECKSTYLE:OFF */
        } catch (Exception e) {
        /* CHECKSTYLE:ON */
            throw new LifecycleException(e);
        }
    }

    @Override
    public void dispose() throws LifecycleException {
        mBeanService.unregister(counter);
        btm.shutdown();
    }
}