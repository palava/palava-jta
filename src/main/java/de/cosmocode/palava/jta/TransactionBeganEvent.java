package de.cosmocode.palava.jta;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * Event interface after {@link UserTransaction#begin()}.
 *
 * @since 1.1 
 * @author Willi Schoenborn
 */
public interface TransactionBeganEvent {

    /**
     * Event callback called after {@link UserTransaction#begin()}.
     * 
     * @since 1.1
     * @param tx the current transaction
     * @throws SystemException if event handling failed
     */
    void eventTransactionBegan(UserTransaction tx) throws SystemException;
    
}
