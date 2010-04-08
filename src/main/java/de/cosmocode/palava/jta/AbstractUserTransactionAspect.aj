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

import javax.transaction.Status;
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
    Object around() throws Exception: transactional() {
        final UserTransaction tx = provider.get();
        LOG.trace("Using transaction {}", tx);
        final boolean localTx = tx.getStatus() == Status.STATUS_NO_TRANSACTION;

        if (localTx) {
            LOG.trace("Beginning automatic transaction");
            tx.begin();
        } else {
            LOG.trace("Transaction already active");
        }

        final Object returnValue;
        
        try {
            returnValue = proceed();
        } catch (RuntimeException e) {
            LOG.error("Exception inside automatic transaction context", e);
            tx.rollback();
            throw e;
        }
        
        try {
            if (localTx && tx.getStatus() == Status.STATUS_ACTIVE) {
                tx.commit();
                LOG.trace("Committed automatic transaction");
            }
            return returnValue;
        } catch (Exception e) {
            LOG.error("Commit in automatic transaction context failed", e);
            tx.rollback();
            throw e;
        }
    }
    
}
