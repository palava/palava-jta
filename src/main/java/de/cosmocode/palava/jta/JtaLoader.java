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

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import de.cosmocode.palava.jmx.MBeanService;
import de.cosmocode.palava.jndi.JndiContextBinderUtility;

/**
 * Binds the Bitronix JTA provider as the JTA manager.
 * 
 * @author Tobias Sarnowski
 */
final class JtaLoader implements Initializable, Disposable {    
    private static final Logger LOG = LoggerFactory.getLogger(JtaLoader.class);

    private JndiContextBinderUtility jndiContextBinderUtility;
    private MBeanService mBeanService;
    private JtaProvider jtaProvider;

    private String manager = JtaConfig.DEFAULT_MANAGER;
    private String user = JtaConfig.DEFAULT_USER;

    private final TransactionCounter counter = new TransactionCounter();

    @Inject
    public JtaLoader(
        JndiContextBinderUtility jndiContextBinderUtility,
        MBeanService mBeanService,
        JtaProvider jtaProvider) {

        this.jndiContextBinderUtility = jndiContextBinderUtility;
        this.mBeanService = mBeanService;
        this.jtaProvider = jtaProvider;
    }

    @Inject(optional = true)
    void setManager(@Named(JtaConfig.MANAGER) String manager) {
        this.manager = Preconditions.checkNotNull(manager, "Manager");
    }

    @Inject(optional = true)
    void setUser(@Named(JtaConfig.USER) String user) {
        this.user = Preconditions.checkNotNull(user, "User");
    }

    @Override
    public void initialize() throws LifecycleException {
        try {
            final TransactionManager tm = new TransactionManagerCounter(jtaProvider.getTransactionManager(), counter);
            final UserTransaction tx = new UserTransactionCounter(jtaProvider.getUserTransaction(), counter);

            LOG.info("Binding TransactionManager to {} [{}]", manager, tm);
            jndiContextBinderUtility.bind(manager, tm, TransactionManager.class);

            LOG.info("Binding UserTransaction to {} [{}]", user, tx);
            jndiContextBinderUtility.bind(user, tx, UserTransaction.class);

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
    }
    
}
