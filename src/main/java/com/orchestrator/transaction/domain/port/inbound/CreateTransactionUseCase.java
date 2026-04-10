package com.orchestrator.transaction.domain.port.inbound;

import com.orchestrator.transaction.application.dto.CreateTransactionCommand;
import com.orchestrator.transaction.application.dto.TransactionResult;

public interface CreateTransactionUseCase {
    TransactionResult createTransaction(CreateTransactionCommand command);
}
