package com.orchestrator.transaction.domain.validator;

import com.orchestrator.transaction.application.dto.CreateTransactionCommand;
import com.orchestrator.transaction.application.dto.CustomerDto;
import com.orchestrator.transaction.domain.exception.InvalidFormatException;
import com.orchestrator.transaction.domain.exception.MissingFieldException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.regex.Pattern;

public class TransactionValidator {

    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern COUNTRY_PATTERN = Pattern.compile("^[A-Z]{2}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}$");
    private static final Pattern DIGITS_PATTERN = Pattern.compile("^\\d+$");

    public static void validate(CreateTransactionCommand command) {
        if (command == null) {
            throw new MissingFieldException("El comando de transacción no puede ser nulo");
        }

        // 1. Validaciones de presencia (MissingFieldException - 001)
        validateRequiredString(command.getClientTransactionId(), "clientTransactionId");
        if (command.getAmount() == null) {
            throw new MissingFieldException("amount es obligatorio");
        }
        validateRequiredString(command.getCurrency(), "currency");
        validateRequiredString(command.getCountry(), "country");
        validateRequiredString(command.getPaymentMethodId(), "paymentMethodId");
        validateRequiredString(command.getWebhookUrl(), "webhookUrl");
        validateRequiredString(command.getRedirectUrl(), "redirectUrl");

        if (command.getCustomer() == null) {
            throw new MissingFieldException("customer es obligatorio");
        }

        CustomerDto customer = command.getCustomer();
        validateRequiredString(customer.getDocumentType(), "customer.documentType");
        validateRequiredString(customer.getDocumentNumber(), "customer.documentNumber");
        validateRequiredString(customer.getEmail(), "customer.email");
        validateRequiredString(customer.getFirstName(), "customer.firstName");
        validateRequiredString(customer.getLastName(), "customer.lastName");

        // 2. Validaciones de formato (InvalidFormatException - 002)

        // Longitud máxima para los campos de texto (<= 255 caracteres)
        validateMaxLength(command.getClientTransactionId(), 255, "clientTransactionId");
        validateMaxLength(command.getCurrency(), 255, "currency");
        validateMaxLength(command.getCountry(), 255, "country");
        validateMaxLength(command.getPaymentMethodId(), 255, "paymentMethodId");
        validateMaxLength(command.getWebhookUrl(), 255, "webhookUrl");
        validateMaxLength(command.getRedirectUrl(), 255, "redirectUrl");
        validateMaxLength(command.getDescription(), 255, "description");

        validateMaxLength(customer.getDocumentType(), 255, "customer.documentType");
        validateMaxLength(customer.getDocumentNumber(), 255, "customer.documentNumber");
        validateMaxLength(customer.getEmail(), 255, "customer.email");
        validateMaxLength(customer.getFirstName(), 255, "customer.firstName");
        validateMaxLength(customer.getLastName(), 255, "customer.lastName");
        validateMaxLength(customer.getMiddleName(), 255, "customer.middleName");
        validateMaxLength(customer.getSecondLastName(), 255, "customer.secondLastName");
        validateMaxLength(customer.getCountryCallingCode(), 255, "customer.countryCallingCode");
        validateMaxLength(customer.getPhoneNumber(), 255, "customer.phoneNumber");

        // Formatos específicos
        if (command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidFormatException("amount debe ser mayor a 0");
        }

        if (!CURRENCY_PATTERN.matcher(command.getCurrency()).matches()) {
            throw new InvalidFormatException("currency debe contener exactamente 3 letras mayúsculas");
        }

        if (!COUNTRY_PATTERN.matcher(command.getCountry()).matches()) {
            throw new InvalidFormatException("country debe contener exactamente 2 letras mayúsculas");
        }

        validateUrl(command.getWebhookUrl(), "webhookUrl");
        validateUrl(command.getRedirectUrl(), "redirectUrl");

        if (command.getExpirationTime() != null && command.getExpirationTime().isBefore(Instant.now())) {
            throw new InvalidFormatException("expirationTime debe ser una fecha futura");
        }

        if (!EMAIL_PATTERN.matcher(customer.getEmail()).matches()) {
            throw new InvalidFormatException("email tiene un formato inválido");
        }

        if (customer.getCountryCallingCode() != null && !customer.getCountryCallingCode().trim().isEmpty()) {
            if (!DIGITS_PATTERN.matcher(customer.getCountryCallingCode()).matches()) {
                throw new InvalidFormatException("countryCallingCode debe contener solo dígitos");
            }
        }

        if (customer.getPhoneNumber() != null && !customer.getPhoneNumber().trim().isEmpty()) {
            if (!DIGITS_PATTERN.matcher(customer.getPhoneNumber()).matches()) {
                throw new InvalidFormatException("phoneNumber debe contener solo dígitos");
            }
        }
    }

    private static void validateRequiredString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new MissingFieldException(fieldName + " es obligatorio");
        }
    }

    private static void validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new InvalidFormatException(fieldName + " no debe exceder los " + maxLength + " caracteres");
        }
    }

    private static void validateUrl(String url, String fieldName) {
        if (url != null && !url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            throw new InvalidFormatException(fieldName + " debe comenzar con http:// o https://");
        }
    }
}
