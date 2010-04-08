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
