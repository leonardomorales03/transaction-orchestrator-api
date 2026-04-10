package com.orchestrator.transaction.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    // Identificadores
    private UUID transactionId;
    private String clientTransactionId;

    // Detalles financieros
    private BigDecimal amount;
    private String currency;
    private String country;

    // Configuración de pago
    private String paymentMethodId;
    private String webhookUrl;
    private String redirectUrl;

    // Datos adicionales
    private String description;
    private Instant expirationTime;

    // Estado y auditoría
    private TransactionStatus status;
    private Instant processedAt;

    // Relaciones
    private Customer customer;
}
