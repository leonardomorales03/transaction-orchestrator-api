package com.orchestrator.transaction.domain.exception;

public class ProviderException extends DomainException {
    public ProviderException(String message) {
        super("005", message);
    }
}