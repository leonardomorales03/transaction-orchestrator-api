package com.orchestrator.transaction.infrastructure.adapter.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.orchestrator.transaction.application.dto.CustomerDto;
import lombok.Data;


import java.time.Instant;

@Data
public class CreateTransactionRequest {
    
    @JsonProperty("client_transaction_id")
    private String clientTransactionId;
    
    private Long amount;
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
    
    private CustomerDto customer;
}
