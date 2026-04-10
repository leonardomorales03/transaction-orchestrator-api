package com.orchestrator.transaction.application.dto;

public enum ResponseCode {
    SUCCESS("000", "Successful operation"),
    MISSING_FIELD("001", "Campo obligatorio ausente"),
    INVALID_FORMAT("002", "Formato de datos inválido"),
    NOT_FOUND("003", "Transacción no encontrada"),
    UNSUPPORTED_PROVIDER("004", "payment_method_id no soportado"),
    PROVIDER_ERROR("005", "Error del proveedor de pago"),
    INTERNAL_ERROR("999", "Error interno no clasificado");

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}