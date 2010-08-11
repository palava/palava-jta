package de.cosmocode.palava.jta;

import javax.transaction.UserTransaction;

/**
 * Event interface after {@link UserTransaction#rollback()}.
 *
 * @since 1.1 
 * @author Willi Schoenborn
 */
public interface TransactionRolledbackEvent {

    /**
     * Event callback called after {@link UserTransaction#rollback()}.
     * 
     * @since 1.1
     * @param tx the current transaction
     */
    void eventTransactionRolledback(UserTransaction tx);
    
}
