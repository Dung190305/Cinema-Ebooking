package com.cinemaebooking.backend.coupon.application.dto;
import com.cinemaebooking.backend.coupon.domain.enums.CouponType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class UpdateDraftCouponRequest implements CouponFullUpdateRequest {
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
