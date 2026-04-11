package com.orchestrator.transaction.infrastructure.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.orchestrator.transaction.domain.model.Transaction;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    
    @JsonProperty("transaction_id")
    private UUID transactionId;
    
    @JsonProperty("processed_at")
    private Instant processedAt;
    
    @JsonProperty("client_transaction_id")
    private String clientTransactionId;
    
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    
    private String currency;
    private String country;
    private String description;

    public static TransactionResponse fromDomain(Transaction tx) {
        return TransactionResponse.builder()
                .transactionId(tx.getTransactionId())
                .processedAt(tx.getProcessedAt())
                .clientTransactionId(tx.getClientTransactionId())
                .paymentMethodId(tx.getPaymentMethodId())
                .currency(tx.getCurrency())
                .country(tx.getCountry())
                .description(tx.getDescription())
                .build();
    }
}
