package com.orchestrator.transaction.infrastructure.adapter.outbound.persistence.repository;

import com.orchestrator.transaction.domain.model.TransactionStatus;
import com.orchestrator.transaction.infrastructure.adapter.outbound.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

    Optional<TransactionEntity> findByTransactionId(UUID transactionId);

    @Modifying
    @Query("UPDATE TransactionEntity t SET t.status = :status WHERE t.transactionId = :transactionId")
    void updateStatus(@Param("transactionId") UUID transactionId, @Param("status") TransactionStatus status);
}
