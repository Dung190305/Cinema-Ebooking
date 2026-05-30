package com.cinemaebooking.backend.user_coupon.application.mapper;

import com.cinemaebooking.backend.coupon.application.port.CouponRepository;
import com.cinemaebooking.backend.coupon.domain.model.Coupon;
import com.cinemaebooking.backend.coupon.domain.valueobject.CouponId;
import com.cinemaebooking.backend.user_coupon.application.dto.UserCouponResponse;
import com.cinemaebooking.backend.user_coupon.domain.model.UserCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserCouponResponseMapper {
    private final CouponRepository couponRepository;

    public UserCouponResponse toResponse(UserCoupon userCoupon) {
        if (userCoupon == null) return null;
        Coupon coupon = couponRepository.findById(CouponId.of(userCoupon.getCouponId()))
                .orElse(null);
        return new UserCouponResponse(
                userCoupon.getId() != null ? userCoupon.getId().getValue() : null,
                userCoupon.getUserId(),
                userCoupon.getCouponId(),
                coupon != null ? coupon.getCode() : null,
                coupon != null ? coupon.getType() : null,
                coupon != null ? coupon.getValue() : null,
                coupon != null ? coupon.getMinimumBookingValue() : null,
                coupon != null ? coupon.getMaximumDiscountAmount() : null,
                userCoupon.getReceivedAt(),
                userCoupon.getUsageRemain(),
                userCoupon.getUsedAt(),
                userCoupon.getExpiredAt(),
                userCoupon.getStatus()
        );
    }
}