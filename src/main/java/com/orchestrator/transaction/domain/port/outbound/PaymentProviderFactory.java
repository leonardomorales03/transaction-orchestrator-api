package com.orchestrator.transaction.domain.port.outbound;

public interface PaymentProviderFactory {
    PaymentProviderPort getProvider(String paymentMethodId);
}
