package com.orchestrator.transaction.infrastructure.adapter.outbound.persistence.entity;

import com.orchestrator.transaction.domain.model.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id", updatable = false, nullable = false)
    private UUID transactionId;

    @Column(name = "client_transaction_id", nullable = false, unique = true)
    private String clientTransactionId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private Long amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "country", nullable = false, length = 2)
    private String country;

    @Column(name = "payment_method_id", nullable = false)
    private String paymentMethodId;

    @Column(name = "webhook_url", nullable = false)
    private String webhookUrl;

    @Column(name = "redirect_url", nullable = false)
    private String redirectUrl;

    @Column(name = "description")
    private String description;

    @Column(name = "expiration_time")
    private Instant expirationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TransactionStatus status;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;
}
