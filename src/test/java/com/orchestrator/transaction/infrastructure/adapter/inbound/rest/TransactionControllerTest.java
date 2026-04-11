package com.orchestrator.transaction.infrastructure.adapter.inbound.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.orchestrator.transaction.application.dto.CustomerDto;
import com.orchestrator.transaction.application.dto.TransactionResult;
import com.orchestrator.transaction.domain.exception.*;
import com.orchestrator.transaction.domain.model.Customer;
import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.domain.model.TransactionStatus;
import com.orchestrator.transaction.domain.port.inbound.CreateTransactionUseCase;
import com.orchestrator.transaction.domain.port.inbound.GetTransactionUseCase;
import com.orchestrator.transaction.infrastructure.adapter.inbound.rest.dto.CreateTransactionRequest;
import com.orchestrator.transaction.infrastructure.adapter.inbound.rest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {TransactionController.class, GlobalExceptionHandler.class})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateTransactionUseCase createTransactionUseCase;

    @MockBean
    private GetTransactionUseCase getTransactionUseCase;

    private CreateTransactionRequest validRequest;

    @BeforeEach
    void setUp() {
        // Necesario para que ObjectMapper sepa cómo serializar/deserializar java.time.Instant
        objectMapper.registerModule(new JavaTimeModule());

        CustomerDto customer = CustomerDto.builder()
                .documentType("CC")
                .documentNumber("123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        validRequest = new CreateTransactionRequest();
        validRequest.setClientTransactionId("txn-123");
        validRequest.setAmount(new BigDecimal("100.00"));
        validRequest.setCurrency("USD");
        validRequest.setCountry("US");
        validRequest.setPaymentMethodId("VISA");
        validRequest.setWebhookUrl("http://hook");
        validRequest.setRedirectUrl("http://redirect");
        validRequest.setCustomer(customer);
    }

    @Test
    void createTransaction_Success_Returns200And000() throws Exception {
        TransactionResult result = TransactionResult.builder()
                .transactionId(UUID.randomUUID())
                .clientTransactionId("txn-123")
                .status(TransactionStatus.PROCESSING)
                .build();

        when(createTransactionUseCase.createTransaction(any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("000"))
                .andExpect(jsonPath("$.data.client_transaction_id").value("txn-123"));
    }

    @Test
    void createTransaction_MissingField_Returns400And001() throws Exception {
        when(createTransactionUseCase.createTransaction(any()))
                .thenThrow(new MissingFieldException("amount es obligatorio"));

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value("001"))
                .andExpect(jsonPath("$.message").value("amount es obligatorio"));
    }

    @Test
    void createTransaction_InvalidFormat_Returns422And002() throws Exception {
        when(createTransactionUseCase.createTransaction(any()))
                .thenThrow(new InvalidFormatException("email tiene un formato inválido"));

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.responseCode").value("002"));
    }

    @Test
    void createTransaction_ProviderError_Returns200And005() throws Exception {
        when(createTransactionUseCase.createTransaction(any()))
                .thenThrow(new ProviderException("Fondos insuficientes"));

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("005"));
    }

    @Test
    void getTransaction_Success_Returns200And000() throws Exception {
        UUID id = UUID.randomUUID();
        Customer customer = Customer.builder().documentType("CC").documentNumber("123").build();
        Transaction tx = Transaction.builder()
                .transactionId(id)
                .clientTransactionId("txn-123")
                .status(TransactionStatus.PROCESSING)
                .customer(customer)
                .build();

        when(getTransactionUseCase.getTransaction(id)).thenReturn(tx);

        mockMvc.perform(get("/api/v1/transactions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("000"))
                .andExpect(jsonPath("$.data.client_transaction_id").value("txn-123"));
    }

    @Test
    void getTransaction_NotFound_Returns404And003() throws Exception {
        UUID id = UUID.randomUUID();
        when(getTransactionUseCase.getTransaction(id))
                .thenThrow(new NotFoundException("No encontrada"));

        mockMvc.perform(get("/api/v1/transactions/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.responseCode").value("003"));
    }
}
