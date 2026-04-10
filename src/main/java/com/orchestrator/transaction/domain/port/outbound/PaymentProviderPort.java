package com.orchestrator.transaction.domain.port.outbound;

import com.orchestrator.transaction.domain.model.Transaction;

public interface PaymentProviderPort {
    ProviderResponse process(Transaction transaction);
    
    String supportedPaymentMethodId();
}
