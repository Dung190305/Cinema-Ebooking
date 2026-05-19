package com.cinemaebooking.backend.coupon.application.dto;

import com.cinemaebooking.backend.coupon.domain.enums.CouponType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCouponRequest implements CouponFullUpdateRequest {

    private String code;
    private CouponType type;
    private BigDecimal value;
    private Integer usageLimit;
    private Integer perUserUsage;
    private Integer pointsToRedeem;
    private BigDecimal minimumBookingValue;
    private BigDecimal maximumDiscountAmount;
    private LocalDate startDate;
    private LocalDate endDate;
}