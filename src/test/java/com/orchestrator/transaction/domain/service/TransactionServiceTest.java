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
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderFactory;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderPort;
import com.orchestrator.transaction.domain.port.outbound.ProviderResponse;
import com.orchestrator.transaction.domain.port.outbound.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentProviderFactory paymentProviderFactory;

    @Mock
    private PaymentProviderPort paymentProviderPort;

    @InjectMocks
    private TransactionService transactionService;

    private CreateTransactionCommand validCommand;
    private Transaction sampleTransaction;
    private UUID sampleId;

    @BeforeEach
    void setUp() {
        sampleId = UUID.randomUUID();

        CustomerDto customerDto = CustomerDto.builder()
                .documentType("CC")
                .documentNumber("123456789")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        validCommand = CreateTransactionCommand.builder()
                .clientTransactionId("txn-123")
                .amount(10000L)
                .currency("USD")
                .country("US")
                .paymentMethodId("VISA")
                .webhookUrl("https://webhook.site/test")
                .redirectUrl("https://example.com/redirect")
                .customer(customerDto)
                .build();

        Customer customer = Customer.builder()
                .documentType("CC")
                .documentNumber("123456789")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        sampleTransaction = Transaction.builder()
                .transactionId(sampleId)
                .clientTransactionId("txn-123")
                .amount(10000L)
                .currency("USD")
                .country("US")
                .paymentMethodId("VISA")
                .webhookUrl("https://webhook.site/test")
                .redirectUrl("https://example.com/redirect")
                .status(TransactionStatus.PENDING)
                .processedAt(Instant.now())
                .customer(customer)
                .build();
    }

    @Test
    @DisplayName("Debe crear la transacción y retornar PROCESSING si el proveedor es exitoso")
    void shouldCreateTransactionSuccessfully() {
        
        when(paymentProviderFactory.getProvider("VISA")).thenReturn(paymentProviderPort);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentProviderPort.process(any(Transaction.class))).thenReturn(new ProviderResponse(true, "Aprobado"));

        TransactionResult result = transactionService.createTransaction(validCommand);

        assertNotNull(result);
        assertEquals(TransactionStatus.PROCESSING, result.getStatus());
        assertEquals("txn-123", result.getClientTransactionId());
        assertNotNull(result.getTransactionId());

        // Verificar que se actualizó el estado
        verify(transactionRepository).updateStatus(result.getTransactionId(), TransactionStatus.PROCESSING);
    }

    @Test
    @DisplayName("Debe fallar y lanzar ProviderException si el proveedor retorna error lógico")
    void shouldThrowProviderExceptionWhenProviderReturnsFalse() {
        // Arrange
        when(paymentProviderFactory.getProvider("VISA")).thenReturn(paymentProviderPort);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentProviderPort.process(any(Transaction.class))).thenReturn(new ProviderResponse(false, "Fondos insuficientes"));
        
        ProviderException exception = assertThrows(ProviderException.class, 
                () -> transactionService.createTransaction(validCommand));

        assertEquals("005", exception.getResponseCode());
        assertEquals("Fondos insuficientes", exception.getMessage());

        // Verificar que la BD se actualizó a FAILED
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(transactionRepository).updateStatus(idCaptor.capture(), eq(TransactionStatus.FAILED));
    }

    @Test
    @DisplayName("Debe fallar y lanzar ProviderException si el proveedor lanza una RuntimeException")
    void shouldThrowProviderExceptionWhenProviderCrashes() {
        // Arrange
        when(paymentProviderFactory.getProvider("VISA")).thenReturn(paymentProviderPort);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentProviderPort.process(any(Transaction.class))).thenThrow(new RuntimeException("Timeout del proveedor"));
        
        ProviderException exception = assertThrows(ProviderException.class, 
                () -> transactionService.createTransaction(validCommand));

        assertEquals("005", exception.getResponseCode());
        assertTrue(exception.getMessage().contains("Error de comunicación con el proveedor de pago"));
        assertTrue(exception.getMessage().contains("Timeout del proveedor"));

        // Verificar que la BD se actualizó a FAILED
        verify(transactionRepository).updateStatus(any(UUID.class), eq(TransactionStatus.FAILED));
    }

    @Test
    @DisplayName("Debe fallar y lanzar DomainException (999) si falla la persistencia inicial")
    void shouldThrowDomainExceptionOnInitialPersistenceFailure() {
        // Arrange
        when(paymentProviderFactory.getProvider("VISA")).thenReturn(paymentProviderPort);
        when(transactionRepository.save(any(Transaction.class))).thenThrow(new RuntimeException("DB Connection Refused"));

        DomainException exception = assertThrows(DomainException.class, 
                () -> transactionService.createTransaction(validCommand));

        assertEquals("999", exception.getResponseCode());
        assertTrue(exception.getMessage().contains("DB Connection Refused"));

        // Verificar que NO se llamó al proveedor
        verify(paymentProviderPort, never()).process(any());
    }

    @Test
    @DisplayName("Debe retornar la transacción si el ID existe en BD")
    void shouldReturnTransactionWhenIdExists() {
        
        when(transactionRepository.findById(sampleId)).thenReturn(Optional.of(sampleTransaction));
        
        Transaction found = transactionService.getTransaction(sampleId);

        assertNotNull(found);
        assertEquals(sampleId, found.getTransactionId());
        assertEquals("txn-123", found.getClientTransactionId());
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException si el ID no existe en BD")
    void shouldThrowNotFoundExceptionWhenIdDoesNotExist() {
        
        when(transactionRepository.findById(sampleId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, 
                () -> transactionService.getTransaction(sampleId));

        assertEquals("003", exception.getResponseCode());
        assertTrue(exception.getMessage().contains("Transacción no encontrada"));
    }
}