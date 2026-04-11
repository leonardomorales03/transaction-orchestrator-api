package com.orchestrator.transaction.infrastructure.adapter.outbound.persistence.mapper;

import com.orchestrator.transaction.domain.model.Customer;
import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.infrastructure.adapter.outbound.persistence.entity.CustomerEntity;
import com.orchestrator.transaction.infrastructure.adapter.outbound.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    // De Dominio a Entidad JPA (Para Guardar) 

    public TransactionEntity toEntity(Transaction domain) {
        if (domain == null) {
            return null;
        }

        return TransactionEntity.builder()
                .transactionId(domain.getTransactionId())
                .clientTransactionId(domain.getClientTransactionId())
                .amount(domain.getAmount())
                .currency(domain.getCurrency())
                .country(domain.getCountry())
                .paymentMethodId(domain.getPaymentMethodId())
                .webhookUrl(domain.getWebhookUrl())
                .redirectUrl(domain.getRedirectUrl())
                .description(domain.getDescription())
                .expirationTime(domain.getExpirationTime())
                .status(domain.getStatus())
                .processedAt(domain.getProcessedAt())
                .customer(toCustomerEntity(domain.getCustomer()))
                .build();
    }

    private CustomerEntity toCustomerEntity(Customer domain) {
        if (domain == null) {
            return null;
        }

        return CustomerEntity.builder()
                .documentType(domain.getDocumentType())
                .documentNumber(domain.getDocumentNumber())
                .email(domain.getEmail())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .middleName(domain.getMiddleName())
                .secondLastName(domain.getSecondLastName())
                .countryCallingCode(domain.getCountryCallingCode())
                .phoneNumber(domain.getPhoneNumber())
                // El campo 'id' y 'createdAt' son manejados por la base de datos (Hibernate)
                .build();
    }

    // De Entidad JPA a Dominio (Para Leer) 

    public Transaction toDomain(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        return Transaction.builder()
                .transactionId(entity.getTransactionId())
                .clientTransactionId(entity.getClientTransactionId())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .country(entity.getCountry())
                .paymentMethodId(entity.getPaymentMethodId())
                .webhookUrl(entity.getWebhookUrl())
                .redirectUrl(entity.getRedirectUrl())
                .description(entity.getDescription())
                .expirationTime(entity.getExpirationTime())
                .status(entity.getStatus())
                .processedAt(entity.getProcessedAt())
                .customer(toCustomerDomain(entity.getCustomer()))
                .build();
    }

    private Customer toCustomerDomain(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }

        return Customer.builder()
                .documentType(entity.getDocumentType())
                .documentNumber(entity.getDocumentNumber())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .middleName(entity.getMiddleName())
                .secondLastName(entity.getSecondLastName())
                .countryCallingCode(entity.getCountryCallingCode())
                .phoneNumber(entity.getPhoneNumber())
                .build();
    }
}
