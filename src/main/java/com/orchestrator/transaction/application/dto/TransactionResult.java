package com.orchestrator.transaction.application.dto;

import com.orchestrator.transaction.domain.model.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private UUID transactionId;
    private String clientTransactionId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private Instant processedAt;
}
