package com.orchestrator.transaction.infrastructure.adapter.outbound.persistence.adapter;

import com.orchestrator.transaction.domain.model.Transaction;
import com.orchestrator.transaction.domain.model.TransactionStatus;
import com.orchestrator.transaction.domain.port.outbound.TransactionRepository;
import com.orchestrator.transaction.infrastructure.adapter.outbound.persistence.entity.TransactionEntity;
import com.orchestrator.transaction.infrastructure.adapter.outbound.persistence.mapper.TransactionMapper;
import com.orchestrator.transaction.infrastructure.adapter.outbound.persistence.repository.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionMapper mapper;

    @Override
    @Transactional
    public Transaction save(Transaction transaction) {
        // 1. Mapeamos del Dominio puro hacia Entidad JPA
        TransactionEntity entity = mapper.toEntity(transaction);
        
        // 2. Guardamos en Base de Datos usando Spring Data JPA
        TransactionEntity savedEntity = jpaRepository.save(entity);
        
        // 3. Mapeamos la Entidad JPA devuelta hacia nuestro Dominio puro
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> findById(UUID transactionId) {
        // 1. Buscamos en Base de Datos
        Optional<TransactionEntity> entityOptional = jpaRepository.findByTransactionId(transactionId);
        
        // 2. Si lo encuentra, mapeamos a Dominio, sino devolvemos Optional.empty()
        return entityOptional.map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void updateStatus(UUID transactionId, TransactionStatus status) {
        // Actualizamos el estado directamente en la BD usando nuestra query @Modifying
        jpaRepository.updateStatus(transactionId, status);
    }
}
