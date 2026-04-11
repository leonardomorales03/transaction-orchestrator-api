package com.orchestrator.transaction.domain.validator;

import com.orchestrator.transaction.application.dto.CreateTransactionCommand;
import com.orchestrator.transaction.application.dto.CustomerDto;
import com.orchestrator.transaction.domain.exception.InvalidFormatException;
import net.jqwik.api.*;


import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransactionValidatorInvalidFormatPropertiesTest {

    @Property(tries = 100)
    void invalidFormatFieldsShouldThrowInvalidFormatException(
            @ForAll("invalidFormatCommands") CreateTransactionCommand command) {

        InvalidFormatException exception = assertThrows(
                InvalidFormatException.class,
                () -> TransactionValidator.validate(command)
        );

        assertEquals("002", exception.getResponseCode());
    }

    @Provide
    Arbitrary<CreateTransactionCommand> invalidFormatCommands() {
        return Arbitraries.of(
                createCommandWithInvalidFormat("amount_zero"),
                createCommandWithInvalidFormat("amount_negative"),
                
                createCommandWithInvalidFormat("currency_lowercase"),
                createCommandWithInvalidFormat("currency_toolong"),
                createCommandWithInvalidFormat("currency_numbers"),
                
                createCommandWithInvalidFormat("country_lowercase"),
                createCommandWithInvalidFormat("country_toolong"),
                createCommandWithInvalidFormat("country_numbers"),
                
                createCommandWithInvalidFormat("webhook_invalid_scheme"),
                createCommandWithInvalidFormat("redirect_invalid_scheme"),
                
                createCommandWithInvalidFormat("expiration_past"),
                
                createCommandWithInvalidFormat("email_no_at"),
                createCommandWithInvalidFormat("email_no_domain"),
                
                createCommandWithInvalidFormat("phone_with_letters"),
                createCommandWithInvalidFormat("calling_code_with_letters"),
                
                createCommandWithInvalidFormat("too_long_string")
        );
    }

    private CreateTransactionCommand createCommandWithInvalidFormat(String invalidType) {
        CreateTransactionCommand command = createValidBaseCommand();
        CustomerDto customer = command.getCustomer();

        switch (invalidType) {
            case "amount_zero" -> command.setAmount(0L);
            case "amount_negative" -> command.setAmount(-50L);
            
            case "currency_lowercase" -> command.setCurrency("usd");
            case "currency_toolong" -> command.setCurrency("USDD");
            case "currency_numbers" -> command.setCurrency("US1");
            
            case "country_lowercase" -> command.setCountry("us");
            case "country_toolong" -> command.setCountry("USA");
            case "country_numbers" -> command.setCountry("U1");
            
            case "webhook_invalid_scheme" -> command.setWebhookUrl("ftp://webhook.site");
            case "redirect_invalid_scheme" -> command.setRedirectUrl("www.example.com");
            
            case "expiration_past" -> command.setExpirationTime(Instant.now().minus(1, ChronoUnit.DAYS));
            
            case "email_no_at" -> customer.setEmail("testexample.com");
            case "email_no_domain" -> customer.setEmail("test@");
            
            case "phone_with_letters" -> customer.setPhoneNumber("123A456");
            case "calling_code_with_letters" -> customer.setCountryCallingCode("+57");
            
            case "too_long_string" -> command.setClientTransactionId("a".repeat(256));
        }

        return command;
    }

    private CreateTransactionCommand createValidBaseCommand() {
        CustomerDto customer = CustomerDto.builder()
                .documentType("CC")
                .documentNumber("123456789")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        return CreateTransactionCommand.builder()
                .clientTransactionId("txn-123")
                .amount(10000L)
                .currency("USD")
                .country("US")
                .paymentMethodId("VISA")
                .webhookUrl("https://webhook.site/test")
                .redirectUrl("https://example.com/redirect")
                .customer(customer)
                .build();
    }
}