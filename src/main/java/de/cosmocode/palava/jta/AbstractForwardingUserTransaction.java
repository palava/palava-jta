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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.*;

/**
 * @author Tobias Sarnowski
 */
public abstract class AbstractForwardingUserTransaction implements UserTransaction {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractForwardingUserTransaction.class);

    private final UserTransaction userTransaction;

    protected AbstractForwardingUserTransaction(UserTransaction userTransaction) {
        this.userTransaction = Preconditions.checkNotNull(userTransaction, "UserTransaction");
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        userTransaction.begin();
    }

    @Override
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        userTransaction.commit();
    }

    @Override
    public int getStatus() throws SystemException {
        return userTransaction.getStatus();
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        userTransaction.rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        userTransaction.setRollbackOnly();
    }

    @Override
    public void setTransactionTimeout(int i) throws SystemException {
        userTransaction.setTransactionTimeout(i);
    }

    @Override
    public String toString() {
        return "{Forwarding:" + userTransaction.toString() + "}";
    }
}