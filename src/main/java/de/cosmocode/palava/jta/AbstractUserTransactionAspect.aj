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

import de.cosmocode.palava.core.aop.AbstractPalavaAspect;

public abstract aspect AbstractUserTransactionAspect extends AbstractPalavaAspect issingleton() {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractUserTransactionAspect.class);

    private Provider<UserTransaction> provider;
    
    @Inject
    void setProvider(Provider<UserTransaction> provider) {
        this.provider = Preconditions.checkNotNull(provider, "Provider");
    }
    
    protected abstract pointcut transactional();
    
    @SuppressAjWarnings("adviceDidNotMatch")
    Object around(): transactional() {
        final UserTransaction tx = provider.get();
        LOG.trace("Using transaction {}", tx);

        try {
            LOG.trace("Status of transaction: {}", tx.getStatus());
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }
        
        final boolean local;
        
        try {
            local = tx.getStatus() == Status.STATUS_NO_TRANSACTION;
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }

        if (local) {
            LOG.debug("Beginning automatic transaction {}", tx);
            try {
                tx.begin();
            } catch (NotSupportedException e) {
                throw new IllegalStateException(e);
            } catch (SystemException e) {
                throw new IllegalStateException(e);
            }
        } else {
            LOG.trace("Transaction {} already active", tx);
        }

        final Object returnValue;
        
        try {
            returnValue = proceed();
        } catch (Exception e) {
            try {
                if (local && 
                    (tx.getStatus() == Status.STATUS_ACTIVE || tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)) {
                    LOG.info("Rolling back local/active transaction {}", tx);
                    tx.rollback();
                } else if (tx.getStatus() != Status.STATUS_ROLLEDBACK || tx.getStatus() != Status.STATUS_ROLLING_BACK) {
                    LOG.debug("Setting transaction {} as rollback only", tx);
                    tx.setRollbackOnly();
                }
            } catch (Exception inner) {
                LOG.error("Rollback failed", inner);
            }
            throw new IllegalStateException(e);
        }
        
        final int status;
        
        try {
            status = tx.getStatus();
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }
        
        if (local) {
            if (status == Status.STATUS_MARKED_ROLLBACK) {
                try {
                    LOG.debug("Rolling back marked transaction {}", tx);
                    tx.rollback();
                } catch (SystemException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                try {
                    tx.commit();
                    LOG.debug("Committed automatic transaction {}", tx);
                } catch (Exception e) {
                    try {
                        LOG.info("Rolling back transaction {}", tx);
                        tx.rollback();
                    } catch (Exception inner) {
                        LOG.error("Rollback failed", inner);
                    }
                    throw new IllegalStateException(e);
                }
            }
        }
        
        return returnValue;
    }
    
}
