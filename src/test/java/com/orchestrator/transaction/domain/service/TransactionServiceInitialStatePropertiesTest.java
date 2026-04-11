package com.orchestrator.transaction.domain.service;

import com.orchestrator.transaction.application.dto.CreateTransactionCommand;
import com.orchestrator.transaction.application.dto.CustomerDto;
import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.domain.model.TransactionStatus;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderFactory;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderPort;
import com.orchestrator.transaction.domain.port.outbound.ProviderResponse;
import com.orchestrator.transaction.domain.port.outbound.TransactionRepository;
import net.jqwik.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionServiceInitialStatePropertiesTest {

    @Property(tries = 100)
    void transactionShouldHaveUUIDv4AndPendingStatusBeforeProvider(
            @ForAll("validTransactionCommands") CreateTransactionCommand command) {
        
        // Arrange
        TransactionRepository repositoryMock = Mockito.mock(TransactionRepository.class);
        PaymentProviderFactory factoryMock = Mockito.mock(PaymentProviderFactory.class);
        PaymentProviderPort providerMock = Mockito.mock(PaymentProviderPort.class);
        
        TransactionService service = new TransactionService(repositoryMock, factoryMock);

        // Simulamos que el proveedor existe y retorna éxito
        when(factoryMock.getProvider(command.getPaymentMethodId())).thenReturn(providerMock);
        when(providerMock.process(any(Transaction.class))).thenReturn(new ProviderResponse(true, "Success"));
        
        // Usamos un capturador para interceptar la Transaction exacta que se pasa al repository.save()
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        when(repositoryMock.save(transactionCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        service.createTransaction(command);
        Transaction capturedTransaction = transactionCaptor.getValue();
        
        // 1. Verificamos que el estado inicial al momento de guardar (antes de procesar) sea PENDING
        assertEquals(TransactionStatus.PENDING, capturedTransaction.getStatus());
        
        // 2. Verificamos que se haya generado un UUID
        UUID generatedId = capturedTransaction.getTransactionId();
        assertEquals(4, generatedId.version(), "El UUID generado debe ser versión 4");
    }

    @Provide
    Arbitrary<CreateTransactionCommand> validTransactionCommands() {
        // Generadores para crear comandos 100% válidos aleatorios
        Arbitrary<String> clientTxnIdArb = Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(20);
        Arbitrary<BigDecimal> amountArb = Arbitraries.bigDecimals().between(new BigDecimal("1.00"), new BigDecimal("10000.00"));
        Arbitrary<String> currencyArb = Arbitraries.strings().withCharRange('A', 'Z').ofLength(3);
        Arbitrary<String> countryArb = Arbitraries.strings().withCharRange('A', 'Z').ofLength(2);
        Arbitrary<String> providerArb = Arbitraries.of("VISA", "MASTERCARD", "PSE");

        return Combinators.combine(
                clientTxnIdArb, amountArb, currencyArb, countryArb, providerArb
        ).as((clientTxnId, amount, currency, country, provider) -> {
            
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
                    .currency(currency)
                    .country(country)
                    .paymentMethodId(provider)
                    .webhookUrl("https://webhook.site/test")
                    .redirectUrl("https://example.com/redirect")
                    .customer(customer)
                    .build();
        });
    }
}