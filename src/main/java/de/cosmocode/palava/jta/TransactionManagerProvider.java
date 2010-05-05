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

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * Provider for {@link TransactionManager}.
 * 
 * @author Tobias Sarnowski
 */
public class TransactionManagerProvider implements Provider<TransactionManager> {
    
    private static final Logger LOG = LoggerFactory.getLogger(TransactionManagerProvider.class);

    private final Provider<Context> provider;
    
    private String manager = JtaConfig.DEFAULT_MANAGER;

    @Inject
    public TransactionManagerProvider(Provider<Context> provider) {
        this.provider = Preconditions.checkNotNull(provider, "Provider");
    }

    @Inject(optional = true)
    public void setManager(@Named(JtaConfig.MANAGER) String manager) {
        this.manager = Preconditions.checkNotNull(manager, "Manager");
    }

    @Override
    public TransactionManager get() {
        final Context context = provider.get();

        try {
            LOG.trace("Looking for TransactionManager in {}", manager);
            final Object tm = context.lookup(manager);
            assert tm instanceof TransactionManager : String.format("%s should be a TransactionManager", tm);
            return TransactionManager.class.cast(tm);
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }
}
