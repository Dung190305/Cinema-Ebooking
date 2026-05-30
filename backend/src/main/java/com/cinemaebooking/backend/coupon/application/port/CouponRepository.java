package com.cinemaebooking.backend.coupon.application.port;

import com.cinemaebooking.backend.coupon.domain.enums.CouponStatus;
import com.cinemaebooking.backend.coupon.domain.model.Coupon;
import com.cinemaebooking.backend.coupon.domain.valueobject.CouponId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface CouponRepository {

    Coupon create(Coupon coupon);

    Coupon update(Coupon coupon);

    Coupon updateDraft(Coupon coupon);

    void updateStatus(Coupon coupon);

    void updateRemainingUsage(Coupon coupon);

    Optional<Coupon> findById(CouponId id);

    Optional<Coupon> findByCode(String code);

    Page<Coupon> findAll(Pageable pageable);

    void deleteById(CouponId id);

    boolean existsById(CouponId id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, CouponId id);

    Page<Coupon> findByStatusAndEndDateAfter(CouponStatus status, LocalDate endDate, Pageable pageable);
}