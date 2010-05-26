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

/**
 * Provider interface for jta {@link TransactionManager}
 * and {@link UserTransaction}.
 * 
 * @author Tobias Sarnowski
 */
public interface JtaProvider {

    /**
     * Retrieves the transaction manager.
     * 
     * @return the transaction manager object to bind to jndi
     */
    TransactionManager getTransactionManager();

    /**
     * Retrieves the user transaction.
     * 
     * @return the user transaction object to bind to jndi
     */
    UserTransaction getUserTransaction();

}
