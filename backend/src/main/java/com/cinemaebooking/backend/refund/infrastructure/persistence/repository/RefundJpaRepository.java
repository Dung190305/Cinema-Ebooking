package com.cinemaebooking.backend.refund.infrastructure.persistence.repository;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import com.cinemaebooking.backend.refund.infrastructure.persistence.entity.RefundJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundJpaRepository extends SoftDeleteJpaRepository<RefundJpaEntity> {

    Optional<RefundJpaEntity> findByBooking_IdAndDeletedFalse(Long bookingId);

    boolean existsByBooking_IdAndDeletedFalse(Long bookingId);

    @EntityGraph(attributePaths = {"booking", "booking.user"})
    @Query("""
            SELECT r FROM RefundJpaEntity r
            LEFT JOIN FETCH r.booking b
            LEFT JOIN FETCH b.user
            WHERE r.deleted = false
              AND (:status IS NULL OR r.status = :status)
            """)
    Page<RefundJpaEntity> findAllWithBooking(
            @Param("status") RefundStatus status,
            Pageable pageable
    );
}
