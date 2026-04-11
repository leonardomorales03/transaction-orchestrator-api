package com.orchestrator.transaction.infrastructure.adapter.outbound.provider;

import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderPort;
import com.orchestrator.transaction.domain.port.outbound.ProviderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProviderBAdapter implements PaymentProviderPort {

    @Override
    public ProviderResponse process(Transaction transaction) {
        log.info("Enviando transacción a Provider B (PSE)... ID: {}, Monto: {}", 
                transaction.getTransactionId(), transaction.getAmount());
        
        simulateNetworkDelay();

        log.info("Respuesta recibida de Provider B: Aprobada");
        return new ProviderResponse(true, "Transaction approved by Provider B");
    }

    @Override
    public String supportedPaymentMethodId() {
        return "PSE";
    }

    private void simulateNetworkDelay() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
