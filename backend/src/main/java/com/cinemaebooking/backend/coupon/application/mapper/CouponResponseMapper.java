package com.cinemaebooking.backend.coupon.application.mapper;

import com.cinemaebooking.backend.coupon.application.dto.CouponResponse;
import com.cinemaebooking.backend.coupon.domain.model.Coupon;
import org.springframework.stereotype.Component;

@Component
public class CouponResponseMapper {

    public CouponResponse toResponse(Coupon coupon) {
        if (coupon == null) return null;

        return new CouponResponse(
                coupon.getId() != null ? coupon.getId().getValue() : null,
                coupon.getCode(),
                coupon.getType(),
                coupon.getValue(),
                coupon.getUsageLimit(),
                coupon.getRemainingUsage(),
                coupon.getPerUserUsage(),
                coupon.getPointsToRedeem(),
                coupon.getMinimumBookingValue(),
                coupon.getMaximumDiscountAmount(),
                coupon.getStartDate(),
                coupon.getEndDate(),
                coupon.getEffectiveStatus()
        );
    }
}