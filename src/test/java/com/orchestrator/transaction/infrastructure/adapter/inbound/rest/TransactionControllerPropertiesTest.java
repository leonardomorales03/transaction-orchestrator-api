package com.orchestrator.transaction.infrastructure.adapter.inbound.rest;

import com.orchestrator.transaction.application.dto.ApiResponse;
import com.orchestrator.transaction.application.dto.CustomerDto;
import com.orchestrator.transaction.application.dto.TransactionResult;
import com.orchestrator.transaction.domain.exception.*;
import com.orchestrator.transaction.domain.model.TransactionStatus;
import com.orchestrator.transaction.domain.port.inbound.CreateTransactionUseCase;
import com.orchestrator.transaction.domain.port.inbound.GetTransactionUseCase;
import com.orchestrator.transaction.infrastructure.adapter.inbound.rest.dto.CreateTransactionRequest;
import com.orchestrator.transaction.infrastructure.adapter.inbound.rest.exception.GlobalExceptionHandler;
import net.jqwik.api.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;


import java.time.Instant;
import java.util.UUID;

public class TransactionControllerPropertiesTest {

    @Property(tries = 100)
    void validRequestProducesStructuredSuccessResponse(@ForAll("validRequests") CreateTransactionRequest request) {
        CreateTransactionUseCase createMock = Mockito.mock(CreateTransactionUseCase.class);
        GetTransactionUseCase getMock = Mockito.mock(GetTransactionUseCase.class);
        TransactionController controller = new TransactionController(createMock, getMock);

        TransactionResult mockResult = TransactionResult.builder()
                .transactionId(UUID.randomUUID())
                .clientTransactionId(request.getClientTransactionId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(TransactionStatus.PROCESSING)
                .processedAt(Instant.now())
                .build();

        Mockito.when(createMock.createTransaction(Mockito.any())).thenReturn(mockResult);

        ResponseEntity<ApiResponse<TransactionResult>> response = controller.createTransaction(request);

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertNotNull(response.getBody());
        
        ApiResponse<TransactionResult> body = response.getBody();
        Assertions.assertEquals("000", body.getResponseCode(), "El código de respuesta debe ser 000");
        Assertions.assertNotNull(body.getMessage(), "El mensaje no debe ser nulo");
        Assertions.assertNotNull(body.getData(), "La data no debe ser nula");
        
        Assertions.assertEquals(mockResult.getTransactionId(), body.getData().getTransactionId());
        Assertions.assertEquals(TransactionStatus.PROCESSING, body.getData().getStatus());
    }

    @Property(tries = 100)
    void allResponsesContainResponseCodeAndMessage(@ForAll("randomExceptions") Exception ex) {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<ApiResponse<Void>> response;

        if (ex instanceof MissingFieldException e) response = handler.handleMissingFieldException(e);
        else if (ex instanceof InvalidFormatException e) response = handler.handleInvalidFormatException(e);
        else if (ex instanceof NotFoundException e) response = handler.handleNotFoundException(e);
        else if (ex instanceof UnsupportedPaymentMethodException e) response = handler.handleUnsupportedPaymentMethodException(e);
        else if (ex instanceof ProviderException e) response = handler.handleProviderException(e);
        else if (ex instanceof DomainException e) response = handler.handleGenericDomainException(e);
        else response = handler.handleAllUncaughtException(ex);

        Assertions.assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        
        ApiResponse<Void> body = response.getBody();
        
        Assertions.assertNotNull(body.getResponseCode(), "responseCode no puede ser nulo");
        Assertions.assertFalse(body.getResponseCode().trim().isEmpty(), "responseCode no puede estar vacío");
        
        Assertions.assertNotNull(body.getMessage(), "message no puede ser nulo");
        Assertions.assertFalse(body.getMessage().trim().isEmpty(), "message no puede estar vacío");
    }

    @Provide
    Arbitrary<CreateTransactionRequest> validRequests() {
        Arbitrary<String> stringArb = Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(15);
        Arbitrary<Long> amountArb = Arbitraries.longs().between(1L, 10000000L);
        
        return Combinators.combine(stringArb, amountArb).as((str, amount) -> {
            CustomerDto customer = new CustomerDto();
            customer.setDocumentType("CC");
            customer.setDocumentNumber("123");
            customer.setEmail("test@test.com");
            customer.setFirstName("John");
            customer.setLastName("Doe");

            CreateTransactionRequest req = new CreateTransactionRequest();
            req.setClientTransactionId(str);
            req.setAmount(amount);
            req.setCurrency("USD");
            req.setCountry("US");
            req.setPaymentMethodId("VISA");
            req.setWebhookUrl("http://hook");
            req.setRedirectUrl("http://redirect");
            req.setCustomer(customer);
            return req;
        });
    }

    @Provide
    Arbitrary<Exception> randomExceptions() {
        return Arbitraries.of(
                new MissingFieldException("missing"),
                new InvalidFormatException("invalid"),
                new NotFoundException("not found"),
                new UnsupportedPaymentMethodException("unsupported"),
                new ProviderException("provider error"),
                new DomainException("999", "internal") {},
                new RuntimeException("unexpected")
        );
    }
}
