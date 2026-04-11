package com.orchestrator.transaction.domain.exception;

public class MissingFieldException extends ValidationException {
    public MissingFieldException(String message) {
        super("001", message);
    }
}