package com.orchestrator.transaction.infrastructure.config;

import com.orchestrator.transaction.domain.port.inbound.CreateTransactionUseCase;
import com.orchestrator.transaction.domain.port.inbound.GetTransactionUseCase;
import com.orchestrator.transaction.domain.port.outbound.PaymentProviderFactory;
import com.orchestrator.transaction.domain.port.outbound.TransactionRepository;
import com.orchestrator.transaction.domain.service.TransactionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public TransactionService transactionService(
            TransactionRepository transactionRepository,
            PaymentProviderFactory paymentProviderFactory
    ) {
        return new TransactionService(transactionRepository, paymentProviderFactory);
    }

    // Opcionalmente, podemos exponer las interfaces inbound explícitamente 
    // como beans si algún componente solo requiere consultar o crear.
    // Sin embargo, TransactionService ya implementa ambas interfaces.
}
