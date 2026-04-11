package com.orchestrator.transaction.domain.service;

import com.orchestrator.transaction.application.dto.CreateTransactionCommand;
import com.orchestrator.transaction.application.dto.CustomerDto;
import com.orchestrator.transaction.domain.exception.DomainException;
import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderFactory;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderPort;
import com.orchestrator.transaction.domain.port.outbound.TransactionRepository;
import net.jqwik.api.*;
import org.mockito.Mockito;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

public class TransactionServicePersistenceFailurePropertiesTest {

    @Property(tries = 100)
    void persistenceFailureShouldPreventProviderInvocation(
            @ForAll("validTransactionCommands") CreateTransactionCommand command) {

        TransactionRepository repositoryMock = Mockito.mock(TransactionRepository.class);
        PaymentProviderFactory factoryMock = Mockito.mock(PaymentProviderFactory.class);
        PaymentProviderPort providerMock = Mockito.mock(PaymentProviderPort.class);

        TransactionService service = new TransactionService(repositoryMock, factoryMock);

        // Configuramos la Factory para que devuelva nuestro Mock del proveedor
        when(factoryMock.getProvider(command.getPaymentMethodId())).thenReturn(providerMock);

        // Simulamos un fallo crítico de persistencia en la base de datos
        when(repositoryMock.save(any(Transaction.class)))
                .thenThrow(new RuntimeException("Database connection timeout"));

        DomainException exception = assertThrows(
                DomainException.class,
                () -> service.createTransaction(command)
        );

        // 1. Verificamos que el código de respuesta sea el "999" (Error Interno)
        assertEquals("999", exception.getResponseCode());
        
        // 2. LA PROPIEDAD MÁS IMPORTANTE: Verificamos que el proveedor NUNCA fue llamado
        // Si el proveedor hubiera sido llamado, le estaríamos cobrando al cliente sin tener registro en BD.
        verify(providerMock, never()).process(any(Transaction.class));
    }

    @Provide
    Arbitrary<CreateTransactionCommand> validTransactionCommands() {
        Arbitrary<String> clientTxnIdArb = Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(20);
        Arbitrary<Long> amountArb = Arbitraries.longs().between(1L, 10000000L);
        Arbitrary<String> providerArb = Arbitraries.of("VISA", "MASTERCARD", "PSE");

        return Combinators.combine(
                clientTxnIdArb, amountArb, providerArb
        ).as((clientTxnId, amount, provider) -> {
            
            CustomerDto customer = CustomerDto.builder()
                    .documentType("CC")
                    .documentNumber("123456789")
                    .email("test@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            return CreateTransactionCommand.builder()
                    .clientTransactionId(clientTxnId)
                    .amount(amount)
                    .currency("USD")
                    .country("US")
                    .paymentMethodId(provider)
                    .webhookUrl("https://webhook.site/test")
                    .redirectUrl("https://example.com/redirect")
                    .customer(customer)
                    .build();
        });
    }
}