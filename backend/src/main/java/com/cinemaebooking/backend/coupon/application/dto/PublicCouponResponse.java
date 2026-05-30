package com.cinemaebooking.backend.coupon.application.dto;

import com.cinemaebooking.backend.coupon.domain.enums.CouponStatus;
import com.cinemaebooking.backend.coupon.domain.enums.CouponType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PublicCouponResponse {
    private Long id;
    private String code;
    private CouponType type;
    private BigDecimal value;
    private Integer perUserUsage;
    private Integer pointsToRedeem;
    private BigDecimal minimumBookingValue;
    private BigDecimal maximumDiscountAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private CouponStatus status;
    private Integer remainingSlots;
}
