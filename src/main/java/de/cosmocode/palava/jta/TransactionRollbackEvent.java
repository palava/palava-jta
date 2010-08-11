package de.cosmocode.palava.jta;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * Event interface before {@link UserTransaction#rollback()}.
 *
 * @since 1.1 
 * @author Willi Schoenborn
 */
public interface TransactionRollbackEvent {

    /**
     * Event callback called before {@link UserTransaction#rollback()}.
     * 
     * @since 1.1
     * @param tx the current transaction
     * @throws SystemException if event handling failed
     */
    void eventTransactionRollback(UserTransaction tx) throws SystemException;

}
