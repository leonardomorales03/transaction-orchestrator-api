package com.orchestrator.transaction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.Instant;

/**
 * DTO que representa el comando para crear una transacción.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionCommand {
    private String clientTransactionId;
    private Long amount;
    private String currency;
    private String country;
    private String paymentMethodId;
    private String webhookUrl;
    private String redirectUrl;
    private String description;
    private Instant expirationTime;
    private CustomerDto customer;
}
