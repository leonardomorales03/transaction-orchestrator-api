package com.orchestrator.transaction.domain.exception;

public abstract class DomainException extends RuntimeException {
    private final String responseCode;

    public DomainException(String responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    public String getResponseCode() {
        return responseCode;
    }
}