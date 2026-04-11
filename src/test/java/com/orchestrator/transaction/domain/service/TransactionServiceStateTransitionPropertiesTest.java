package com.orchestrator.transaction.domain.service;

import com.orchestrator.transaction.application.dto.CreateTransactionCommand;
import com.orchestrator.transaction.application.dto.CustomerDto;
import com.orchestrator.transaction.application.dto.TransactionResult;
import com.orchestrator.transaction.domain.exception.ProviderException;
import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.domain.model.TransactionStatus;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderFactory;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderPort;
import com.orchestrator.transaction.domain.port.outbound.ProviderResponse;
import com.orchestrator.transaction.domain.port.outbound.TransactionRepository;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransactionServiceStateTransitionPropertiesTest {

    @Property(tries = 100)
    void stateTransitionShouldMatchProviderResult(
            @ForAll("validTransactionCommands") CreateTransactionCommand command,
            @ForAll("providerResults") boolean isProviderSuccess) {

        TransactionRepository repositoryMock = Mockito.mock(TransactionRepository.class);
        PaymentProviderFactory factoryMock = Mockito.mock(PaymentProviderFactory.class);
        PaymentProviderPort providerMock = Mockito.mock(PaymentProviderPort.class);

        TransactionService service = new TransactionService(repositoryMock, factoryMock);

        // Simulamos el comportamiento del repositorio de guardar y retornar la misma transacción
        when(repositoryMock.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Configuramos la Factory para que devuelva nuestro Mock del proveedor
        when(factoryMock.getProvider(command.getPaymentMethodId())).thenReturn(providerMock);

        // Configuramos el mock del proveedor para que retorne el valor inyectado aleatoriamente
        ProviderResponse providerResponse = new ProviderResponse(isProviderSuccess, isProviderSuccess ? "Success" : "Declined");
        when(providerMock.process(any(Transaction.class))).thenReturn(providerResponse);

        if (isProviderSuccess) {
            // Si el proveedor tiene éxito, no debería haber excepciones y se retorna el TransactionResult
            TransactionResult result = service.createTransaction(command);
            
            // Verificamos que el resultado devuelto tenga el estado PROCESSING
            assertEquals(TransactionStatus.PROCESSING, result.getStatus());
            
            // Verificamos que se haya llamado a updateStatus en la base de datos con PROCESSING
            verify(repositoryMock).updateStatus(result.getTransactionId(), TransactionStatus.PROCESSING);
        } else {
            // Si el proveedor falla, el servicio debe lanzar una ProviderException
            ProviderException exception = assertThrows(
                    ProviderException.class,
                    () -> service.createTransaction(command)
            );
            
            // Verificamos el mensaje de la excepción y el código "005"
            assertEquals("005", exception.getResponseCode());
            assertEquals("Declined", exception.getMessage());
            
            // Verificamos que se haya llamado a updateStatus en la base de datos con FAILED
            verify(repositoryMock).updateStatus(any(), eq(TransactionStatus.FAILED));
        }
    }

    @Provide
    Arbitrary<Boolean> providerResults() {
        // Generador booleano: true (éxito) o false (error)
        return Arbitraries.of(true, false);
    }

    @Provide
    Arbitrary<CreateTransactionCommand> validTransactionCommands() {
        Arbitrary<String> clientTxnIdArb = Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(20);
        Arbitrary<BigDecimal> amountArb = Arbitraries.bigDecimals().between(new BigDecimal("1.00"), new BigDecimal("10000.00"));
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