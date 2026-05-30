package com.cinemaebooking.backend.user_coupon.infrastructure.adapter;

import com.cinemaebooking.backend.common.exception.domain.CouponExceptions;
import com.cinemaebooking.backend.common.exception.domain.UserCouponExceptions;
import com.cinemaebooking.backend.coupon.application.port.CouponRepository;
import com.cinemaebooking.backend.coupon.domain.model.Coupon;
import com.cinemaebooking.backend.user_coupon.application.port.CouponPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CouponAdapter implements CouponPort {

    private final CouponRepository couponRepository;

    @Override
    public CouponSnapshot findValidCoupon(String code, LocalDateTime now) {
        Coupon coupon = couponRepository.findByCode(code).orElse(null);
        if (coupon == null) {
            throw CouponExceptions.notFound(code);
        }

        if(!coupon.isActive()){
            throw UserCouponExceptions.couponNotActive(coupon.getCode());
        }

        if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(now.toLocalDate())) {
            throw UserCouponExceptions.couponNotActive(coupon.getCode());
        }

        if(coupon.getEndDate().isBefore(now.toLocalDate())){
            throw UserCouponExceptions.couponExpired(coupon.getCode());
        }

        return new CouponSnapshot(coupon.getId().getValue(),coupon.getCode(),true,toDateTime(coupon.getEndDate()),coupon.getPointsToRedeem(),coupon.getPerUserUsage());
    }

    private LocalDateTime toDateTime(LocalDate date) {
        return date == null ? null : LocalDateTime.of(date, LocalTime.MAX);
    }
}