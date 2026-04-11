package com.orchestrator.transaction.infrastructure.adapter.inbound.rest.exception;

import com.orchestrator.transaction.application.dto.ApiResponse;
import com.orchestrator.transaction.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 1. Excepciones del Dominio ---

    @ExceptionHandler(MissingFieldException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingFieldException(MissingFieldException ex) {
        log.warn("Error de campos faltantes: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getResponseCode(), ex.getMessage()));
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFormatException(InvalidFormatException ex) {
        log.warn("Error de formato inválido: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getResponseCode(), ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(NotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getResponseCode(), ex.getMessage()));
    }

    @ExceptionHandler(UnsupportedPaymentMethodException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedPaymentMethodException(UnsupportedPaymentMethodException ex) {
        log.warn("Método de pago no soportado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getResponseCode(), ex.getMessage()));
    }

    @ExceptionHandler(ProviderException.class)
    public ResponseEntity<ApiResponse<Void>> handleProviderException(ProviderException ex) {
        log.error("Error en el proveedor de pago: {}", ex.getMessage());
        // retorna 200 OK pero con response_code 005
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.error(ex.getResponseCode(), ex.getMessage()));
    }

    // Atrapa cualquier otra DomainException genérica (como el error interno 999 de BD)
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericDomainException(DomainException ex) {
        log.error("Error interno del dominio: {}", ex.getMessage());
        HttpStatus status = "999".equals(ex.getResponseCode()) ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getResponseCode(), ex.getMessage()));
    }

    // --- 2. Excepciones del Framework (Spring) ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Error de validación de Spring: {}", ex.getMessage());
        // Fallbacks de validación de Spring Boot mapeados al código "001"
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("001", "Error de validación en los campos enviados"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Cuerpo de la petición ilegible o malformado: {}", ex.getMessage());
        // JSON malformado o campos con tipos incorrectos (ej. string en vez de número)
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("002", "Formato de petición JSON inválido"));
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {
        log.warn("Tipo de argumento inválido en URL: {}", ex.getMessage());
        // Cuando se envía un UUID inválido en el path
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("002", "Formato de identificador inválido en la ruta"));
    }

    // --- 3. Excepción Genérica (Catch-all) ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllUncaughtException(Exception ex) {
        log.error("Error interno no controlado: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("999", "Error interno del servidor"));
    }
}
