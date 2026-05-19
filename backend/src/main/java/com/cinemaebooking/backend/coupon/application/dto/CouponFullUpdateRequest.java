package com.cinemaebooking.backend.coupon.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CouponFullUpdateRequest {

    String getCode();

    Integer getUsageLimit();

    Integer getPerUserUsage();

    Integer getPointsToRedeem();

    BigDecimal getValue();

    BigDecimal getMinimumBookingValue();

    BigDecimal getMaximumDiscountAmount();

    LocalDate getStartDate();

    LocalDate getEndDate();
}