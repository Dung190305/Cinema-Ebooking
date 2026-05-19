package com.cinemaebooking.backend.coupon.application.port;

import com.cinemaebooking.backend.coupon.domain.model.Coupon;
import com.cinemaebooking.backend.coupon.domain.valueobject.CouponId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CouponRepository {

    Coupon create(Coupon coupon);

    Coupon update(Coupon coupon);

    Coupon updateDraft(Coupon coupon);

    void updateStatus(Coupon coupon);

    Optional<Coupon> findById(CouponId id);

    Page<Coupon> findAll(Pageable pageable);

    void deleteById(CouponId id);

    boolean existsById(CouponId id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, CouponId id);
}