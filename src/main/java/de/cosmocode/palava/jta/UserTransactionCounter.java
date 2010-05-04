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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.*;

/**
 * @author Tobias Sarnowski
 */
public class UserTransactionCounter extends AbstractForwardingUserTransaction {
    private static final Logger LOG = LoggerFactory.getLogger(UserTransactionCounter.class);

    private TransactionCounter counter;

    protected UserTransactionCounter(UserTransaction userTransaction, TransactionCounter counter) {
        super(userTransaction);
        this.counter = counter;
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        super.begin();
        counter.getPending().incrementAndGet();
    }

    @Override
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        super.commit();
        counter.getPending().decrementAndGet();
        counter.getCommitted().incrementAndGet();
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        try {
            super.rollback();
            counter.getRolledbackSuccess().incrementAndGet();
        } catch (Exception e) {
            counter.getRolledbackFailed().incrementAndGet();
        } finally {
            counter.getPending().decrementAndGet();
        }
    }

    @Override
    public String toString() {
        return "{" + super.toString() + ":COUNTED:p" + counter.getPending() + ":c" + counter.getCommitted() + ":r" + counter.getRolledback() + "}";
    }
}