package com.orchestrator.transaction.domain.service;

import com.orchestrator.transaction.application.dto.CreateTransactionCommand;
import com.orchestrator.transaction.application.dto.CustomerDto;
import com.orchestrator.transaction.domain.exception.UnsupportedPaymentMethodException;
import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderFactory;
import com.orchestrator.transaction.domain.port.outbound.TransactionRepository;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransactionServiceUnsupportedProviderPropertiesTest {

    @Property(tries = 100)
    void unsupportedPaymentMethodShouldThrowExceptionAndPreventPersistence(
            @ForAll("validCommandsWithRandomProviders") CreateTransactionCommand command) {

        // Arrange
        TransactionRepository repositoryMock = Mockito.mock(TransactionRepository.class);
        PaymentProviderFactory factoryMock = Mockito.mock(PaymentProviderFactory.class);

        TransactionService service = new TransactionService(repositoryMock, factoryMock);

        // Simulamos que la Factory no encuentra el proveedor y lanza la excepción esperada
        when(factoryMock.getProvider(command.getPaymentMethodId()))
                .thenThrow(new UnsupportedPaymentMethodException("Proveedor no soportado: " + command.getPaymentMethodId()));

        UnsupportedPaymentMethodException exception = assertThrows(
                UnsupportedPaymentMethodException.class,
                () -> service.createTransaction(command)
        );

        // 1. Verificamos que el código de respuesta sea el "004" (payment_method_id no soportado)
        assertEquals("004", exception.getResponseCode());

        // 2. Verificamos que no se intente persistir en BD si el proveedor no existe
        verify(repositoryMock, never()).save(any(Transaction.class));
    }

    @Provide
    Arbitrary<CreateTransactionCommand> validCommandsWithRandomProviders() {
        Arbitrary<String> clientTxnIdArb = Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(20);
        Arbitrary<BigDecimal> amountArb = Arbitraries.bigDecimals().between(new BigDecimal("1.00"), new BigDecimal("10000.00"));
        
        // Generamos Strings completamente aleatorios para simular métodos de pago no soportados o mal escritos
        Arbitrary<String> randomProviderArb = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(15);

        return Combinators.combine(
                clientTxnIdArb, amountArb, randomProviderArb
        ).as((clientTxnId, amount, randomProvider) -> {
            
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
                    .paymentMethodId(randomProvider)
                    .webhookUrl("https://webhook.site/test")
                    .redirectUrl("https://example.com/redirect")
                    .customer(customer)
                    .build();
        });
    }
}