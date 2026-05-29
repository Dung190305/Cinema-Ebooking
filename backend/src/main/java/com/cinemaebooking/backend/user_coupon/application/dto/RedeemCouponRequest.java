package com.cinemaebooking.backend.user_coupon.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RedeemCouponRequest {
    private Long userId;
    private String couponCode;
}