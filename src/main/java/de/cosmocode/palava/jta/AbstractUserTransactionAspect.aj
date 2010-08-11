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

import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;

import de.cosmocode.palava.core.Registry;
import de.cosmocode.palava.core.aop.AbstractPalavaAspect;

public abstract aspect AbstractUserTransactionAspect extends AbstractPalavaAspect issingleton() {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractUserTransactionAspect.class);

    private Provider<UserTransaction> provider;

    private TransactionBeginEvent begin;
    private TransactionBeganEvent began;
    private TransactionCommitEvent commit;
    private TransactionCommittedEvent committed;
    private TransactionRollbackEvent rollback;
    private TransactionRolledbackEvent rolledback;
    
    @Inject
    void setProvider(Provider<UserTransaction> provider) {
        this.provider = Preconditions.checkNotNull(provider, "Provider");
    }
    
    @Inject
    void setRegistry(Registry registry) {
        Preconditions.checkNotNull(registry, "Registry");
        this.begin = registry.proxy(TransactionBeginEvent.class);
        this.began = registry.proxy(TransactionBeganEvent.class);
        this.commit = registry.proxy(TransactionCommitEvent.class);
        this.committed = registry.proxy(TransactionCommittedEvent.class);
        this.rollback = registry.proxy(TransactionRollbackEvent.class);
        this.rolledback = registry.proxy(TransactionRolledbackEvent.class);
    }
    
    protected abstract pointcut transactional();
    
    @SuppressAjWarnings("adviceDidNotMatch")
    Object around(): transactional() {
        Preconditions.checkNotNull(provider, "Provider not injected yet");
        final UserTransaction tx = provider.get();
        LOG.trace("Using transaction {}", tx);

        try {
            LOG.trace("Status of transaction: {}", tx.getStatus());
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }

        int status;
        final boolean local;
        try {
            status = tx.getStatus();
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }

        local = status == Status.STATUS_NO_TRANSACTION;

        if (local) {
            LOG.debug("Beginning automatic transaction {}", tx);
            try {
                begin.eventTransactionBegin(tx);
                tx.begin();
                began.eventTransactionBegan(tx);
            } catch (NotSupportedException e) {
                throw new IllegalStateException(e);
            } catch (SystemException e) {
                throw new IllegalStateException(e);
            }
        } else {
            LOG.trace("Transaction {} already active (Status: {})", tx, status);
        }

        try {
            status = tx.getStatus();
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }

        final Object returnValue;
        
        try {
            LOG.trace("Status before execution: {}", status);
            returnValue = proceed();
        } catch (Exception e) {
            try {
                if (local && 
                    (tx.getStatus() == Status.STATUS_ACTIVE || tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)) {
                    LOG.info("Rolling back local/active transaction {}", tx);
                    rollback(tx);
                } else if (tx.getStatus() != Status.STATUS_ROLLEDBACK || tx.getStatus() != Status.STATUS_ROLLING_BACK) {
                    LOG.debug("Setting transaction {} as rollback only", tx);
                    tx.setRollbackOnly();
                }
            } catch (Exception inner) {
                LOG.error("Rollback failed", inner);
            }
            throw new IllegalStateException(e);
        }
        
        try {
            status = tx.getStatus();
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }

        LOG.trace("Status after execution: {}", status);
        
        if (local) {
            if (status == Status.STATUS_MARKED_ROLLBACK) {
                try {
                    LOG.debug("Rolling back marked transaction {}", tx);
                    rollback(tx);
                } catch (SystemException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                try {
                    commit.eventTransactionCommit(tx);
                    tx.commit();
                    committed.eventTransactionCommitted(tx);
                    LOG.debug("Committed automatic transaction {}", tx);
                } catch (Exception e) {
                    try {
                        LOG.info("Rolling back transaction {}", tx);
                        rollback(tx);
                    } catch (Exception inner) {
                        LOG.error("Rollback failed", inner);
                    }
                    throw new IllegalStateException(e);
                }
            }
        }
        
        return returnValue;
    }
    
    private void rollback(UserTransaction tx) throws SystemException {
        rollback.eventTransactionRollback(tx);
        tx.rollback();
        rolledback.eventTransactionRolledback(tx);
    }
    
}
