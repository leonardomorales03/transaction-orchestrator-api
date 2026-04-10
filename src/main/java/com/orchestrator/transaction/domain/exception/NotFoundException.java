package com.orchestrator.transaction.domain.exception;

public class NotFoundException extends DomainException {
    public NotFoundException(String message) {
        super("003", message);
    }
}