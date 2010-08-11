package de.cosmocode.palava.jta;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * Event interface before {@link UserTransaction#commit()}.
 *
 * @since 1.1 
 * @author Willi Schoenborn
 */
public interface TransactionCommitEvent {

    /**
     * Event callback called before {@link UserTransaction#commit()}.
     * 
     * @since 1.1
     * @param tx the current transaction
     * @throws SystemException if event handling failed
     */
    void eventTransactionCommit(UserTransaction tx) throws SystemException;
    
}
