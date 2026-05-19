package com.cinemaebooking.backend.coupon.infrastructure.persistence.repository;

import com.cinemaebooking.backend.coupon.infrastructure.persistence.entity.CouponJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * CouponJpaRepository - JPA repository for Coupon entity.
 * Responsibility:
 * - Provide data access methods for CouponJpaEntity
 * - Support validation and query operations
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Repository
public interface CouponJpaRepository extends SoftDeleteJpaRepository<CouponJpaEntity> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    Optional<CouponJpaEntity> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndEndDateAfter(String code, LocalDate currentDate);

    boolean existsByCodeIgnoreCaseAndUsageLimitGreaterThan(String code, Integer usedCount);

    boolean existsByCodeIgnoreCaseAndEndDateAfterAndUsageLimitGreaterThan(
            String code, LocalDate currentDate, Integer usedCount);

}