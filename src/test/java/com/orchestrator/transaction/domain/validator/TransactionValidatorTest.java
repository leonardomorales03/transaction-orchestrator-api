package com.orchestrator.transaction.domain.validator;

import com.orchestrator.transaction.application.dto.CreateTransactionCommand;
import com.orchestrator.transaction.application.dto.CustomerDto;
import com.orchestrator.transaction.domain.exception.InvalidFormatException;
import com.orchestrator.transaction.domain.exception.MissingFieldException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class TransactionValidatorTest {

    private CreateTransactionCommand validCommand;

    @BeforeEach
    void setUp() {
        CustomerDto customer = CustomerDto.builder()
                .documentType("CC")
                .documentNumber("123456789")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        validCommand = CreateTransactionCommand.builder()
                .clientTransactionId("txn-123")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .country("US")
                .paymentMethodId("VISA")
                .webhookUrl("https://webhook.site/test")
                .redirectUrl("https://example.com/redirect")
                .customer(customer)
                .build();
    }

    @Test
    @DisplayName("Debe validar un comando completamente válido sin lanzar excepciones")
    void shouldValidateValidCommandSuccessfully() {
        assertDoesNotThrow(() -> TransactionValidator.validate(validCommand));
    }

    @Test
    @DisplayName("Debe lanzar MissingFieldException si el comando es nulo")
    void shouldThrowExceptionWhenCommandIsNull() {
        MissingFieldException exception = assertThrows(MissingFieldException.class, 
                () -> TransactionValidator.validate(null));
        assertEquals("001", exception.getResponseCode());
        assertEquals("El comando de transacción no puede ser nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar MissingFieldException si el customer es nulo")
    void shouldThrowExceptionWhenCustomerIsNull() {
        validCommand.setCustomer(null);
        MissingFieldException exception = assertThrows(MissingFieldException.class, 
                () -> TransactionValidator.validate(validCommand));
        assertEquals("001", exception.getResponseCode());
        assertEquals("customer es obligatorio", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-100.50"})
    @DisplayName("Debe lanzar InvalidFormatException si el monto es menor o igual a cero")
    void shouldThrowExceptionWhenAmountIsInvalid(String invalidAmount) {
        validCommand.setAmount(new BigDecimal(invalidAmount));
        InvalidFormatException exception = assertThrows(InvalidFormatException.class, 
                () -> TransactionValidator.validate(validCommand));
        assertEquals("002", exception.getResponseCode());
        assertEquals("amount debe ser mayor a 0", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"usd", "US", "USDD", "US1"})
    @DisplayName("Debe lanzar InvalidFormatException si el currency no tiene exactamente 3 letras mayúsculas")
    void shouldThrowExceptionWhenCurrencyIsInvalid(String invalidCurrency) {
        validCommand.setCurrency(invalidCurrency);
        InvalidFormatException exception = assertThrows(InvalidFormatException.class, 
                () -> TransactionValidator.validate(validCommand));
        assertEquals("002", exception.getResponseCode());
        assertTrue(exception.getMessage().contains("exactamente 3 letras mayúsculas"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"us", "U", "USA", "U1"})
    @DisplayName("Debe lanzar InvalidFormatException si el country no tiene exactamente 2 letras mayúsculas")
    void shouldThrowExceptionWhenCountryIsInvalid(String invalidCountry) {
        validCommand.setCountry(invalidCountry);
        InvalidFormatException exception = assertThrows(InvalidFormatException.class, 
                () -> TransactionValidator.validate(validCommand));
        assertEquals("002", exception.getResponseCode());
        assertTrue(exception.getMessage().contains("exactamente 2 letras mayúsculas"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ftp://example.com", "www.example.com", "example.com"})
    @DisplayName("Debe lanzar InvalidFormatException si las URLs no inician con http o https")
    void shouldThrowExceptionWhenUrlIsInvalid(String invalidUrl) {
        validCommand.setWebhookUrl(invalidUrl);
        InvalidFormatException exception = assertThrows(InvalidFormatException.class, 
                () -> TransactionValidator.validate(validCommand));
        assertEquals("002", exception.getResponseCode());
        assertTrue(exception.getMessage().contains("debe comenzar con http:// o https://"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"plainaddress", "@missingusername.com", "username@.com", "username@domain..com", "testexample.com", "test@"})
    @DisplayName("Debe lanzar InvalidFormatException si el email tiene formato inválido")
    void shouldThrowExceptionWhenEmailIsInvalid(String invalidEmail) {
        validCommand.getCustomer().setEmail(invalidEmail);
        InvalidFormatException exception = assertThrows(InvalidFormatException.class, 
                () -> TransactionValidator.validate(validCommand),
                "Expected InvalidFormatException for email: " + invalidEmail);
        assertEquals("002", exception.getResponseCode());
        assertEquals("email tiene un formato inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar InvalidFormatException si un texto excede los 255 caracteres")
    void shouldThrowExceptionWhenStringExceedsMaxLength() {
        String longString = "a".repeat(256);
        validCommand.setClientTransactionId(longString);
        
        InvalidFormatException exception = assertThrows(InvalidFormatException.class, 
                () -> TransactionValidator.validate(validCommand));
        assertEquals("002", exception.getResponseCode());
        assertTrue(exception.getMessage().contains("no debe exceder los 255 caracteres"));
    }

    @Test
    @DisplayName("Debe lanzar InvalidFormatException si expirationTime está en el pasado")
    void shouldThrowExceptionWhenExpirationTimeIsInPast() {
        validCommand.setExpirationTime(Instant.now().minus(1, ChronoUnit.HOURS));
        
        InvalidFormatException exception = assertThrows(InvalidFormatException.class, 
                () -> TransactionValidator.validate(validCommand));
        assertEquals("002", exception.getResponseCode());
        assertEquals("expirationTime debe ser una fecha futura", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+57", "57A", " 57 "})
    @DisplayName("Debe lanzar InvalidFormatException si phoneNumber o countryCallingCode contienen caracteres no numéricos")
    void shouldThrowExceptionWhenPhoneContainsNonDigits(String invalidPhoneData) {
        validCommand.getCustomer().setPhoneNumber(invalidPhoneData);
        
        InvalidFormatException exception = assertThrows(InvalidFormatException.class, 
                () -> TransactionValidator.validate(validCommand));
        assertEquals("002", exception.getResponseCode());
        assertEquals("phoneNumber debe contener solo dígitos", exception.getMessage());
    }
}
