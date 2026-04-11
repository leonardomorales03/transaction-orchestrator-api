package com.orchestrator.transaction.domain.exception;

public class InvalidFormatException extends ValidationException {
    public InvalidFormatException(String message) {
        super("002", message);
    }
}