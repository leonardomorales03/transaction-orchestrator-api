package com.orchestrator.transaction.domain.port.outbound;

import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.domain.model.TransactionStatus;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    
    Optional<Transaction> findById(UUID transactionId);
    
    void updateStatus(UUID transactionId, TransactionStatus status);
}
