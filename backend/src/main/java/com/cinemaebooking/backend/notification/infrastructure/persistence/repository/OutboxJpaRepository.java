package com.cinemaebooking.backend.notification.infrastructure.persistence.repository;

import com.cinemaebooking.backend.notification.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.cinemaebooking.backend.notification.infrastructure.persistence.entity.OutboxEventJpaEntity.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OutboxJpaRepository extends JpaRepository<OutboxEventJpaEntity, Long> {

    @Query("""
            SELECT e FROM OutboxEventJpaEntity e
            WHERE e.status = :status
            ORDER BY e.createdAt ASC
            """)
    List<OutboxEventJpaEntity> findPendingEvents(@Param("status") OutboxStatus status, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE OutboxEventJpaEntity e
            SET e.status = :newStatus
            WHERE e.id = :id AND e.status = :currentStatus
            """)
    int updateStatus(@Param("id") Long id,
                     @Param("currentStatus") OutboxStatus currentStatus,
                     @Param("newStatus") OutboxStatus newStatus);

    long countByStatus(OutboxStatus status);
}
