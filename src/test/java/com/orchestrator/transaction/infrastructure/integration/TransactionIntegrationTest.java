package com.orchestrator.transaction.infrastructure.integration;

import com.orchestrator.transaction.application.dto.ApiResponse;
import com.orchestrator.transaction.application.dto.TransactionResult;
import com.orchestrator.transaction.domain.model.TransactionStatus;
import com.orchestrator.transaction.infrastructure.adapter.inbound.rest.dto.CreateTransactionRequest;
import com.orchestrator.transaction.infrastructure.adapter.inbound.rest.dto.TransactionResponse;
import com.orchestrator.transaction.application.dto.CustomerDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Propiedad 7: Round-trip de persistencia — crear y luego consultar retorna los mismos datos")
    void shouldPerformPersistenceRoundTripSuccessfully() {
        
        // --- 1. PREPARACIÓN DE DATOS (Arrange) ---
        String clientTxId = "INT-TEST-" + UUID.randomUUID().toString().substring(0, 8);
        
        CustomerDto customerDto = CustomerDto.builder()
                .documentType("CC")
                .documentNumber("987654321")
                .email("integration@test.com")
                .firstName("Integration")
                .lastName("Test")
                .countryCallingCode("57")
                .phoneNumber("3000000000")
                .build();

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setClientTransactionId(clientTxId);
        request.setAmount(new BigDecimal("500.50"));
        request.setCurrency("COP");
        request.setCountry("CO");
        request.setPaymentMethodId("PSE");
        request.setWebhookUrl("https://my-webhook.com");
        request.setRedirectUrl("https://my-redirect.com");
        request.setDescription("Prueba de integración real");
        request.setCustomer(customerDto);

        // --- 2. CREACIÓN: POST (Act) ---
        ResponseEntity<ApiResponse<TransactionResult>> createResponse = restTemplate.exchange(
                "/api/v1/transactions",
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {}
        );

        // Verificamos que el POST fue exitoso y nos guardamos el UUID generado
        Assertions.assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        Assertions.assertNotNull(createResponse.getBody());
        Assertions.assertEquals("000", createResponse.getBody().getResponseCode());
        
        TransactionResult resultData = createResponse.getBody().getData();
        Assertions.assertNotNull(resultData);
        Assertions.assertEquals(TransactionStatus.PROCESSING, resultData.getStatus());
        
        UUID generatedTransactionId = resultData.getTransactionId();
        Assertions.assertNotNull(generatedTransactionId);

        // --- 3. CONSULTA: GET (Act) ---
        ResponseEntity<ApiResponse<TransactionResponse>> getResponse = restTemplate.exchange(
                "/api/v1/transactions/" + generatedTransactionId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // --- 4. VERIFICACIÓN DEL ROUND-TRIP (Assert) ---
        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertNotNull(getResponse.getBody());
        Assertions.assertEquals("000", getResponse.getBody().getResponseCode());
        
        TransactionResponse fetchedData = getResponse.getBody().getData();
        Assertions.assertNotNull(fetchedData);

        // Validamos que los datos consultados desde la BD sean IDÉNTICOS a los que enviamos
        Assertions.assertEquals(generatedTransactionId, fetchedData.getTransactionId());
        Assertions.assertEquals(request.getClientTransactionId(), fetchedData.getClientTransactionId());
        
        // En Java, BigDecimal(500.50) puede ser devuelto como 500.5000 por la escala de la BD. 
        // compareTo() == 0 ignora la escala y asegura que matemáticamente sean el mismo valor.
        Assertions.assertEquals(0, request.getAmount().compareTo(fetchedData.getAmount()));
        
        Assertions.assertEquals(request.getCurrency(), fetchedData.getCurrency());
        Assertions.assertEquals(request.getCountry(), fetchedData.getCountry());
        Assertions.assertEquals(request.getPaymentMethodId(), fetchedData.getPaymentMethodId());
        Assertions.assertEquals(request.getWebhookUrl(), fetchedData.getWebhookUrl());
        Assertions.assertEquals(request.getRedirectUrl(), fetchedData.getRedirectUrl());
        Assertions.assertEquals(request.getDescription(), fetchedData.getDescription());
        Assertions.assertEquals(TransactionStatus.PROCESSING, fetchedData.getStatus());
        
        // Verificamos que el customer también se guardó y recuperó idénticamente
        CustomerDto fetchedCustomer = fetchedData.getCustomer();
        Assertions.assertNotNull(fetchedCustomer);
        Assertions.assertEquals(customerDto.getDocumentType(), fetchedCustomer.getDocumentType());
        Assertions.assertEquals(customerDto.getDocumentNumber(), fetchedCustomer.getDocumentNumber());
        Assertions.assertEquals(customerDto.getEmail(), fetchedCustomer.getEmail());
        Assertions.assertEquals(customerDto.getFirstName(), fetchedCustomer.getFirstName());
        Assertions.assertEquals(customerDto.getLastName(), fetchedCustomer.getLastName());
        Assertions.assertEquals(customerDto.getCountryCallingCode(), fetchedCustomer.getCountryCallingCode());
        Assertions.assertEquals(customerDto.getPhoneNumber(), fetchedCustomer.getPhoneNumber());
    }

    @Test
    @DisplayName("Propiedad 11: transaction_id inexistente produce response_code 003")
    void shouldReturn003WhenTransactionIdDoesNotExist() {
        
        // 1. Generamos un UUID v4 aleatorio que con seguridad no existe en la base de datos
        UUID nonExistentId = UUID.randomUUID();

        // 2. Hacemos el GET al endpoint
        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/api/v1/transactions/" + nonExistentId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // 3. Verificamos que el API devuelva 404 Not Found y el código de negocio 003
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("003", response.getBody().getResponseCode());
        Assertions.assertNotNull(response.getBody().getMessage());
        Assertions.assertTrue(response.getBody().getMessage().contains("Transacción no encontrada"));
        Assertions.assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("Propiedad 12: transaction_id con formato inválido produce error de validación 002 (Spring TypeMismatch)")
    void shouldReturn002WhenTransactionIdIsInvalidFormat() {
        
        // 1. Enviamos un string que NO es un UUID válido
        String invalidId = "not-a-uuid-format-1234";

        // 2. Hacemos el GET al endpoint
        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/api/v1/transactions/" + invalidId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // 3. Verificamos que devuelva 400 Bad Request y response_code 002
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("002", response.getBody().getResponseCode());
        Assertions.assertNotNull(response.getBody().getMessage());
        Assertions.assertTrue(response.getBody().getMessage().contains("Formato de identificador inválido en la ruta"));
    }

    @Test
    @DisplayName("Propiedad 5 (integración): Toda respuesta contiene response_code y message")
    void shouldAlwaysContainResponseCodeAndMessageInAllEndpoints() {
        
        // 1. GET Inválido
        ResponseEntity<ApiResponse<Void>> getResponse = restTemplate.exchange(
                "/api/v1/transactions/123",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        Assertions.assertNotNull(getResponse.getBody());
        Assertions.assertNotNull(getResponse.getBody().getResponseCode());
        Assertions.assertNotNull(getResponse.getBody().getMessage());

        // 2. POST Inválido (cuerpo vacío)
        ResponseEntity<ApiResponse<Void>> postResponse = restTemplate.exchange(
                "/api/v1/transactions",
                HttpMethod.POST,
                new HttpEntity<>(new CreateTransactionRequest()), // Request totalmente vacío
                new ParameterizedTypeReference<>() {}
        );
        Assertions.assertNotNull(postResponse.getBody());
        Assertions.assertNotNull(postResponse.getBody().getResponseCode());
        Assertions.assertNotNull(postResponse.getBody().getMessage());
    }

    @Test
    @DisplayName("Idempotencia: Enviar el mismo client_transaction_id dos veces falla con error de base de datos")
    void shouldFailWhenSendingSameClientTransactionIdTwice() {
        
        // --- 1. PREPARACIÓN ---
        String duplicateClientTxId = "DUP-TEST-" + UUID.randomUUID().toString().substring(0, 8);
        
        CustomerDto customerDto = CustomerDto.builder()
                .documentType("CC")
                .documentNumber("987654321")
                .email("duplicate@test.com")
                .firstName("Duplicate")
                .lastName("Test")
                .build();

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setClientTransactionId(duplicateClientTxId);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setCountry("US");
        request.setPaymentMethodId("CARD_VISA");
        request.setWebhookUrl("https://my-webhook.com");
        request.setRedirectUrl("https://my-redirect.com");
        request.setCustomer(customerDto);

        // --- 2. PRIMER POST (ÉXITO) ---
        ResponseEntity<ApiResponse<TransactionResult>> firstResponse = restTemplate.exchange(
                "/api/v1/transactions",
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {}
        );
        Assertions.assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        Assertions.assertEquals("000", firstResponse.getBody().getResponseCode());

        // --- 3. SEGUNDO POST CON EL MISMO REQUEST (DEBE FALLAR) ---
        ResponseEntity<ApiResponse<Void>> secondResponse = restTemplate.exchange(
                "/api/v1/transactions",
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {}
        );

        // Como la columna client_transaction_id es UNIQUE en Postgres, 
        // Hibernate lanzará un DataIntegrityViolationException. 
        // Nuestro TransactionService atrapará eso como un fallo de persistencia y 
        // lo convertirá en DomainException("999").
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, secondResponse.getStatusCode());
        Assertions.assertNotNull(secondResponse.getBody());
        Assertions.assertEquals("999", secondResponse.getBody().getResponseCode());
        Assertions.assertTrue(secondResponse.getBody().getMessage().contains("Error interno al guardar"));
    }
}
