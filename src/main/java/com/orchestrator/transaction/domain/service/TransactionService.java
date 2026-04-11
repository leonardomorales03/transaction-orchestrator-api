package com.orchestrator.transaction.domain.service;

import com.orchestrator.transaction.application.dto.CreateTransactionCommand;
import com.orchestrator.transaction.application.dto.CustomerDto;
import com.orchestrator.transaction.application.dto.TransactionResult;
import com.orchestrator.transaction.domain.exception.DomainException;
import com.orchestrator.transaction.domain.exception.NotFoundException;
import com.orchestrator.transaction.domain.exception.ProviderException;
import com.orchestrator.transaction.domain.model.Customer;
import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.domain.model.TransactionStatus;
import com.orchestrator.transaction.domain.port.inbound.CreateTransactionUseCase;
import com.orchestrator.transaction.domain.port.inbound.GetTransactionUseCase;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderFactory;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderPort;
import com.orchestrator.transaction.domain.port.outbound.ProviderResponse;
import com.orchestrator.transaction.domain.port.outbound.TransactionRepository;
import com.orchestrator.transaction.domain.validator.TransactionValidator;

import java.time.Instant;
import java.util.UUID;

public class TransactionService implements CreateTransactionUseCase, GetTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final PaymentProviderFactory paymentProviderFactory;

    public TransactionService(TransactionRepository transactionRepository, PaymentProviderFactory paymentProviderFactory) {
        this.transactionRepository = transactionRepository;
        this.paymentProviderFactory = paymentProviderFactory;
    }

    @Override
    public TransactionResult createTransaction(CreateTransactionCommand command) {
        // 1. Validar el comando (lanza excepciones de dominio 001 o 002 si falla)
        TransactionValidator.validate(command);

        // 2. Obtener y verificar que el proveedor existe (lanza 004 si no existe)
        PaymentProviderPort provider = paymentProviderFactory.getProvider(command.getPaymentMethodId());

        // 3. Construir la entidad de dominio
        Transaction transaction = buildTransactionFromCommand(command);

        // 4. Persistir estado inicial PENDING (lanza error 999 si falla la BD)
        try {
            transaction = transactionRepository.save(transaction);
        } catch (Exception e) {
            // Propagamos cualquier error de infraestructura como un error interno 999
            throw new DomainException("999", "Error interno al guardar la transacción: " + e.getMessage()) {};
        }

        // 5. Invocar al proveedor de pagos
        try {
            ProviderResponse providerResponse = provider.process(transaction);
            
            if (providerResponse.isSuccess()) {
                // En éxito, actualizamos a PROCESSING 
                transaction.setStatus(TransactionStatus.PROCESSING);
                transactionRepository.updateStatus(transaction.getTransactionId(), TransactionStatus.PROCESSING);
                
                return buildResultFromTransaction(transaction);
            } else {
                // Si el proveedor responde explícitamente con un error lógico
                transactionRepository.updateStatus(transaction.getTransactionId(), TransactionStatus.FAILED);
                throw new ProviderException(providerResponse.getMessage());
            }
        } catch (ProviderException e) {
            // Relanzamos la excepción del proveedor tal cual (para no enmascararla con un 999)
            throw e;
        } catch (Exception e) {
            // Si el proveedor se cae (Timeout, red, etc), marcamos como FAILED y lanzamos 005
            transactionRepository.updateStatus(transaction.getTransactionId(), TransactionStatus.FAILED);
            throw new ProviderException("Error de comunicación con el proveedor de pago: " + e.getMessage());
        }
    }

    @Override
    public Transaction getTransaction(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transacción no encontrada con ID: " + transactionId));
    }

    private Transaction buildTransactionFromCommand(CreateTransactionCommand command) {
        CustomerDto customerDto = command.getCustomer();
        
        Customer customer = Customer.builder()
                .documentType(customerDto.getDocumentType())
                .documentNumber(customerDto.getDocumentNumber())
                .email(customerDto.getEmail())
                .firstName(customerDto.getFirstName())
                .lastName(customerDto.getLastName())
                .middleName(customerDto.getMiddleName())
                .secondLastName(customerDto.getSecondLastName())
                .countryCallingCode(customerDto.getCountryCallingCode())
                .phoneNumber(customerDto.getPhoneNumber())
                .build();

        return Transaction.builder()
                .transactionId(UUID.randomUUID()) // UUID v4
                .clientTransactionId(command.getClientTransactionId())
                .amount(command.getAmount())
                .currency(command.getCurrency())
                .country(command.getCountry())
                .paymentMethodId(command.getPaymentMethodId())
                .webhookUrl(command.getWebhookUrl())
                .redirectUrl(command.getRedirectUrl())
                .description(command.getDescription())
                .expirationTime(command.getExpirationTime())
                .status(TransactionStatus.PENDING) // Estado inicial requerido
                .processedAt(Instant.now())
                .customer(customer)
                .build();
    }

    private TransactionResult buildResultFromTransaction(Transaction transaction) {
        return TransactionResult.builder()
                .transactionId(transaction.getTransactionId())
                .clientTransactionId(transaction.getClientTransactionId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .processedAt(transaction.getProcessedAt())
                .build();
    }
}
