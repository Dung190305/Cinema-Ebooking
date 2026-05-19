package com.cinemaebooking.backend.loyalty.infrastructure.persistence.repository;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity.LoyaltyTransactionJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyTransactionJpaRepository extends SoftDeleteJpaRepository<LoyaltyTransactionJpaEntity> {
    List<LoyaltyTransactionJpaEntity> findByLoyaltyAccountIdOrderByChangeDateDesc(Long loyaltyAccountId);

    Optional<LoyaltyTransactionJpaEntity> findByBookingId(Long bookingId);
}
