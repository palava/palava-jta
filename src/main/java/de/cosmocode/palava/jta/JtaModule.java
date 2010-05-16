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

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;

/**
 * Binds the {@link JtaLoader} as eager {@link Singleton}.
 * 
 * @author Tobias Sarnowski
 * @author Willi Schoenborn
 */
public final class JtaModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(JtaLoader.class).asEagerSingleton();
        binder.bind(TransactionManager.class).toProvider(TransactionManagerProvider.class).in(Singleton.class);
        binder.bind(UserTransaction.class).toProvider(UserTransactionProvider.class).in(Singleton.class);
    }
    
}
