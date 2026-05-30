package com.cinemaebooking.backend.refund.infrastructure.persistence.repository;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import com.cinemaebooking.backend.refund.infrastructure.persistence.entity.RefundJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundJpaRepository extends SoftDeleteJpaRepository<RefundJpaEntity> {

    Optional<RefundJpaEntity> findByBooking_IdAndDeletedFalse(Long bookingId);

    boolean existsByBooking_IdAndDeletedFalse(Long bookingId);
}
