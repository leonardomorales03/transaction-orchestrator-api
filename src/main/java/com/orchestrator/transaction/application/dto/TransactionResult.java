package com.orchestrator.transaction.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.orchestrator.transaction.domain.model.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.Instant;
import java.util.UUID;

/**
 * DTO que representa el resultado exitoso de la creación de una transacción.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResult {
    
    @JsonProperty("transaction_id")
    private UUID transactionId;
    
    @JsonProperty("client_transaction_id")
    private String clientTransactionId;
    
    private Long amount;
    private String currency;
    private TransactionStatus status;
    
    @JsonProperty("processed_at")
    private Instant processedAt;
}
