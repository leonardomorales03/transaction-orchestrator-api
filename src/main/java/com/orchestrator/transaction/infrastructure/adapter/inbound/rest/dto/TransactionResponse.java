package com.orchestrator.transaction.infrastructure.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.orchestrator.transaction.application.dto.CustomerDto;
import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.domain.model.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    
    @JsonProperty("transaction_id")
    private UUID transactionId;
    
    @JsonProperty("client_transaction_id")
    private String clientTransactionId;
    
    private BigDecimal amount;
    private String currency;
    private String country;
    
    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    
    @JsonProperty("webhook_url")
    private String webhookUrl;
    
    @JsonProperty("redirect_url")
    private String redirectUrl;
    
    private String description;
    
    @JsonProperty("expiration_time")
    private Instant expirationTime;
    
    private TransactionStatus status;
    
    @JsonProperty("processed_at")
    private Instant processedAt;
    
    private CustomerDto customer;

    public static TransactionResponse fromDomain(Transaction tx) {
        CustomerDto customerDto = CustomerDto.builder()
                .documentType(tx.getCustomer().getDocumentType())
                .documentNumber(tx.getCustomer().getDocumentNumber())
                .email(tx.getCustomer().getEmail())
                .firstName(tx.getCustomer().getFirstName())
                .lastName(tx.getCustomer().getLastName())
                .middleName(tx.getCustomer().getMiddleName())
                .secondLastName(tx.getCustomer().getSecondLastName())
                .countryCallingCode(tx.getCustomer().getCountryCallingCode())
                .phoneNumber(tx.getCustomer().getPhoneNumber())
                .build();

        return TransactionResponse.builder()
                .transactionId(tx.getTransactionId())
                .clientTransactionId(tx.getClientTransactionId())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .country(tx.getCountry())
                .paymentMethodId(tx.getPaymentMethodId())
                .webhookUrl(tx.getWebhookUrl())
                .redirectUrl(tx.getRedirectUrl())
                .description(tx.getDescription())
                .expirationTime(tx.getExpirationTime())
                .status(tx.getStatus())
                .processedAt(tx.getProcessedAt())
                .customer(customerDto)
                .build();
    }
}
