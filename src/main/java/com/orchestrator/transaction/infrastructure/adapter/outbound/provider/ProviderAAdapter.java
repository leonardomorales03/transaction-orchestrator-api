package com.orchestrator.transaction.infrastructure.adapter.outbound.provider;

import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderPort;
import com.orchestrator.transaction.domain.port.outbound.ProviderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProviderAAdapter implements PaymentProviderPort {

    @Override
    public ProviderResponse process(Transaction transaction) {
        log.info("Enviando transacción a Provider A (CARD_VISA)... ID: {}, Monto: {}", 
                transaction.getTransactionId(), transaction.getAmount());
        
        simulateNetworkDelay();

        log.info("Respuesta recibida de Provider A: Aprobada");
        return new ProviderResponse(true, "Transaction approved by Provider A");
    }

    @Override
    public String supportedPaymentMethodId() {
        return "CARD_VISA";
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
