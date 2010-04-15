/**
 * palava - a java-php-bridge
 * Copyright (C) 2007-2010  CosmoCode GmbH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
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
        
        final boolean local;
        
        try {
            local = tx.getStatus() == Status.STATUS_NO_TRANSACTION;
        } catch (SystemException e) {
            throw new IllegalStateException(e);
        }

        if (local) {
            LOG.trace("Beginning automatic transaction {}", tx);
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
                    LOG.trace("Rolling back marked transaction {}", tx);
                    tx.rollback();
                } catch (SystemException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                try {
                    tx.commit();
                    LOG.trace("Committed automatic transaction {}", tx);
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
