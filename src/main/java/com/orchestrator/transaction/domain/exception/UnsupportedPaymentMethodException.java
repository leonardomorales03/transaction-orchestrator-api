package com.orchestrator.transaction.domain.exception;

public class UnsupportedPaymentMethodException extends DomainException {
    public UnsupportedPaymentMethodException(String message) {
        super("004", message);
    }
}