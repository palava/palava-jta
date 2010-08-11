package de.cosmocode.palava.jta;

import javax.transaction.UserTransaction;

/**
 * Event interface before {@link UserTransaction#begin()}.
 *
 * @since 1.1 
 * @author Willi Schoenborn
 */
public interface TransactionBeginEvent {

    /**
     * Event callback called before {@link UserTransaction#begin()}.
     * 
     * @since 1.1
     * @param tx the current transaction
     */
    void eventTransactionBegin(UserTransaction tx);
    
}
