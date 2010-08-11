package de.cosmocode.palava.jta;

import javax.transaction.UserTransaction;

/**
 * Event interface after {@link UserTransaction#commit()}.
 *
 * @since 1.1 
 * @author Willi Schoenborn
 */
public interface TransactionCommittedEvent {

    /**
     * Event callback called after {@link UserTransaction#commit()}.
     * 
     * @since 1.1
     * @param tx the current transaction
     */
    void eventTransactionCommitted(UserTransaction tx);
    
}
