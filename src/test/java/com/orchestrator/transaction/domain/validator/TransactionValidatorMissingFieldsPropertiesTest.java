package com.orchestrator.transaction.domain.validator;

import com.orchestrator.transaction.application.dto.CreateTransactionCommand;
import com.orchestrator.transaction.application.dto.CustomerDto;
import com.orchestrator.transaction.domain.exception.MissingFieldException;
import net.jqwik.api.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransactionValidatorMissingFieldsPropertiesTest {

    @Property(tries = 100)
    void missingRequiredTransactionFieldsShouldThrowMissingFieldException(
            @ForAll("invalidTransactionCommands") CreateTransactionCommand command) {
        
        MissingFieldException exception = assertThrows(
                MissingFieldException.class,
                () -> TransactionValidator.validate(command)
        );

        assertEquals("001", exception.getResponseCode());
    }

    @Provide
    Arbitrary<CreateTransactionCommand> invalidTransactionCommands() {
        return Arbitraries.of(
                createCommandWithMissingField("clientTransactionId"),
                createCommandWithMissingField("amount"),
                createCommandWithMissingField("currency"),
                createCommandWithMissingField("country"),
                createCommandWithMissingField("paymentMethodId"),
                createCommandWithMissingField("webhookUrl"),
                createCommandWithMissingField("redirectUrl"),
                createCommandWithMissingField("customer"),
                createCommandWithMissingCustomerField("documentType"),
                createCommandWithMissingCustomerField("documentNumber"),
                createCommandWithMissingCustomerField("email"),
                createCommandWithMissingCustomerField("firstName"),
                createCommandWithMissingCustomerField("lastName")
        );
    }

    private CreateTransactionCommand createCommandWithMissingField(String missingField) {
        CreateTransactionCommand command = createValidBaseCommand();
        switch (missingField) {
            case "clientTransactionId" -> command.setClientTransactionId(null);
            case "amount" -> command.setAmount(null);
            case "currency" -> command.setCurrency(null);
            case "country" -> command.setCountry(null);
            case "paymentMethodId" -> command.setPaymentMethodId(null);
            case "webhookUrl" -> command.setWebhookUrl(null);
            case "redirectUrl" -> command.setRedirectUrl(null);
            case "customer" -> command.setCustomer(null);
        }
        return command;
    }

    private CreateTransactionCommand createCommandWithMissingCustomerField(String missingField) {
        CreateTransactionCommand command = createValidBaseCommand();
        CustomerDto customer = command.getCustomer();
        switch (missingField) {
            case "documentType" -> customer.setDocumentType(null);
            case "documentNumber" -> customer.setDocumentNumber(null);
            case "email" -> customer.setEmail(null);
            case "firstName" -> customer.setFirstName(null);
            case "lastName" -> customer.setLastName(null);
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
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .country("US")
                .paymentMethodId("VISA")
                .webhookUrl("https://webhook.site/test")
                .redirectUrl("https://example.com/redirect")
                .customer(customer)
                .build();
    }
}