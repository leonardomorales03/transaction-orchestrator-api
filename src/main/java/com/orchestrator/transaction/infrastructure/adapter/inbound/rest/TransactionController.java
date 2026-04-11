package com.orchestrator.transaction.infrastructure.adapter.inbound.rest;

import com.orchestrator.transaction.application.dto.ApiResponse;
import com.orchestrator.transaction.application.dto.CreateTransactionCommand;
import com.orchestrator.transaction.application.dto.TransactionResult;
import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.domain.port.inbound.CreateTransactionUseCase;
import com.orchestrator.transaction.domain.port.inbound.GetTransactionUseCase;
import com.orchestrator.transaction.infrastructure.adapter.inbound.rest.dto.CreateTransactionRequest;
import com.orchestrator.transaction.infrastructure.adapter.inbound.rest.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final GetTransactionUseCase getTransactionUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResult>> createTransaction(@RequestBody CreateTransactionRequest request) {
        
        // Mapeo manual del Request al Command de Dominio
        CreateTransactionCommand command = CreateTransactionCommand.builder()
                .clientTransactionId(request.getClientTransactionId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .country(request.getCountry())
                .paymentMethodId(request.getPaymentMethodId())
                .webhookUrl(request.getWebhookUrl())
                .redirectUrl(request.getRedirectUrl())
                .description(request.getDescription())
                .expirationTime(request.getExpirationTime())
                .customer(request.getCustomer())
                .build();

        // Invocamos el Caso de Uso (que a su vez validará y orquestará)
        TransactionResult result = createTransactionUseCase.createTransaction(command);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(@PathVariable UUID transactionId) {
        
        // Invocamos el Caso de Uso
        Transaction transaction = getTransactionUseCase.getTransaction(transactionId);
        
        // Convertimos el modelo de dominio puro a DTO de respuesta para ocultar info innecesaria
        TransactionResponse responseDto = TransactionResponse.fromDomain(transaction);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
}
