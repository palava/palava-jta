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

import javax.transaction.*;
import java.io.Serializable;

/**
 * @author Tobias Sarnowski
 */
public abstract class AbstractForwardingTransactionManager implements TransactionManager, Serializable {

    private final TransactionManager transactionManager;

    protected AbstractForwardingTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = Preconditions.checkNotNull(transactionManager, "TransactionManager");
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        transactionManager.begin();
    }

    @Override
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        transactionManager.commit();
    }

    @Override
    public int getStatus() throws SystemException {
        return transactionManager.getStatus();
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        return transactionManager.getTransaction();
    }

    @Override
    public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
        transactionManager.resume(transaction);
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        transactionManager.rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        transactionManager.setRollbackOnly();
    }

    @Override
    public void setTransactionTimeout(int i) throws SystemException {
        transactionManager.setTransactionTimeout(i);
    }

    @Override
    public Transaction suspend() throws SystemException {
        return transactionManager.suspend();
    }

    @Override
    public String toString() {
        return "{Forwarding:" + transactionManager.toString() + "}";
    }
}