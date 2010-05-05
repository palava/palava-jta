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
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * {@link Provider} for {@link UserTransaction} which uses the bound {@link Context}
 * to find an instance.
 * 
 * @author Tobias Sarnowski
 * @author Willi Schoenborn
 */
final class UserTransactionProvider implements Provider<UserTransaction> {
    private static final Logger LOG = LoggerFactory.getLogger(UserTransactionProvider.class);
    
    private final Provider<Context> provider;
    private String user = JtaConfig.DEFAULT_USER;

    @Inject
    public UserTransactionProvider(Provider<Context> provider) {
        this.provider = Preconditions.checkNotNull(provider, "Provider");
    }

    @Inject(optional = true)
    public void setUser(@Named(JtaConfig.USER) String user) {
        this.user = Preconditions.checkNotNull(user, "User");
    }

    @Override
    public UserTransaction get() {
        final Context context = provider.get();
        
        try {
            LOG.trace("Looking for UserTransaction in {}", user);
            final Object tx = context.lookup(user);
            assert tx instanceof UserTransaction : String.format("%s should be a UserTransaction", tx);
            return UserTransaction.class.cast(tx);
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
    }
    
}
