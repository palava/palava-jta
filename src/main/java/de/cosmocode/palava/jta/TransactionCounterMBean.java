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

import java.util.concurrent.atomic.AtomicLong;

/**
 * MBean interface for {@link TransactionCounter}.
 * 
 * @author Tobias Sarnowski
 */
public interface TransactionCounterMBean {

    /**
     * Retrieves the amount of pending transactions. A transaction
     * is considered pending after begin, but before commit or rollback.
     * 
     * @since 1.0
     * @return the amount of pending transactions
     */
    AtomicLong getPending();

    /**
     * Retrieves the amount of committed transactions.
     * 
     * @since 1.0
     * @return the amount of committed transactions
     */
    AtomicLong getCommitted();

    /**
     * Retrieves the amount of rolled back transactions. This includes
     * both successfully rolled back and failures.
     * 
     * @since 1.0
     * @return the amount of rolled back transactions
     */
    long getRolledback();
    
    /**
     * Retrieves the amount of successfully rolled back transaction. 
     * 
     * @since 1.0
     * @return the amount of successfully rolled back transactions
     */
    AtomicLong getRolledbackSuccess();

    /**
     * Retrieves the amount of not successfully rolled back transactions.
     * 
     * @since 1.0
     * @return the amount of not successfully rolled back transactions
     */
    AtomicLong getRolledbackFailed();
    
}
