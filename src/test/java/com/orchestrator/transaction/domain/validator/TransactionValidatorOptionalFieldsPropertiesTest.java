package com.orchestrator.transaction.domain.validator;

import com.orchestrator.transaction.application.dto.CreateTransactionCommand;
import com.orchestrator.transaction.application.dto.CustomerDto;
import net.jqwik.api.*;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TransactionValidatorOptionalFieldsPropertiesTest {

    @Property(tries = 100)
    void optionalFieldsShouldNotAffectValidation(
            @ForAll("optionalFieldsCommands") CreateTransactionCommand command) {
        
        // La validación no debería lanzar ninguna excepción
        Assertions.assertDoesNotThrow(() -> TransactionValidator.validate(command));
    }

    @Provide
    Arbitrary<CreateTransactionCommand> optionalFieldsCommands() {
        // Generadores para campos opcionales
        Arbitrary<String> descriptionArb = Arbitraries.strings().ofMinLength(0).ofMaxLength(255).injectNull(0.5);
        Arbitrary<Instant> expirationTimeArb = Arbitraries.of(Instant.now().plus(1, ChronoUnit.DAYS)).injectNull(0.5);
        
        Arbitrary<String> middleNameArb = Arbitraries.strings().ofMinLength(0).ofMaxLength(255).injectNull(0.5);
        Arbitrary<String> secondLastNameArb = Arbitraries.strings().ofMinLength(0).ofMaxLength(255).injectNull(0.5);
        
        // Generadores para countryCallingCode y phoneNumber (solo dígitos o null/vacío)
        Arbitrary<String> countryCallingCodeArb = Arbitraries.strings().numeric().ofMinLength(0).ofMaxLength(10).injectNull(0.5);
        Arbitrary<String> phoneNumberArb = Arbitraries.strings().numeric().ofMinLength(0).ofMaxLength(15).injectNull(0.5);

        return Combinators.combine(
                descriptionArb,
                expirationTimeArb,
                middleNameArb,
                secondLastNameArb,
                countryCallingCodeArb,
                phoneNumberArb
        ).as((description, expirationTime, middleName, secondLastName, countryCallingCode, phoneNumber) -> {
            
            CustomerDto customer = CustomerDto.builder()
                    .documentType("CC")
                    .documentNumber("123456789")
                    .email("test@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    // Inyectamos los campos opcionales generados aleatoriamente
                    .middleName(middleName)
                    .secondLastName(secondLastName)
                    .countryCallingCode(countryCallingCode)
                    .phoneNumber(phoneNumber)
                    .build();

            return CreateTransactionCommand.builder()
                    .clientTransactionId("txn-123")
                    .amount(new BigDecimal("100.00"))
                    .currency("USD")
                    .country("US")
                    .paymentMethodId("VISA")
                    .webhookUrl("https://webhook.site/test")
                    .redirectUrl("https://example.com/redirect")
                    // Inyectamos los campos opcionales generados aleatoriamente
                    .description(description)
                    .expirationTime(expirationTime)
                    .customer(customer)
                    .build();
        });
    }
}
