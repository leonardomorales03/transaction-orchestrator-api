package com.orchestrator.transaction.infrastructure.adapter.outbound.provider;

import com.orchestrator.transaction.domain.exception.UnsupportedPaymentMethodException;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderFactory;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DefaultPaymentProviderFactory implements PaymentProviderFactory {

    private final Map<String, PaymentProviderPort> providersMap;

    public DefaultPaymentProviderFactory(List<PaymentProviderPort> providers) {
        // Agrupamos todos los proveedores en un Map donde la llave es el paymentMethodId que soportan
        this.providersMap = providers.stream()
                .collect(Collectors.toMap(PaymentProviderPort::supportedPaymentMethodId, Function.identity()));
        
        log.info("PaymentProviderFactory inicializada. Proveedores soportados: {}", providersMap.keySet());
    }

    @Override
    public PaymentProviderPort getProvider(String paymentMethodId) {
        return Optional.ofNullable(providersMap.get(paymentMethodId))
                .orElseThrow(() -> {
                    log.warn("Intento de usar proveedor no soportado: {}", paymentMethodId);
                    return new UnsupportedPaymentMethodException("Proveedor de pago no soportado: " + paymentMethodId);
                });
    }
}
