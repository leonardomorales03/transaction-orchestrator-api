package com.orchestrator.transaction.domain.port.inbound;

import com.orchestrator.transaction.domain.model.Transaction;

import java.util.UUID;

public interface GetTransactionUseCase {
    Transaction getTransaction(UUID transactionId);
}
