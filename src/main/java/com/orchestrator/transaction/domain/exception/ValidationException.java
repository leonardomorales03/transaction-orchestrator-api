package com.orchestrator.transaction.domain.exception;

public abstract class ValidationException extends DomainException {
    public ValidationException(String responseCode, String message) {
        super(responseCode, message);
    }
}