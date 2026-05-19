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
public class UpdateCouponRequest {
    private Integer usageLimit;
    private LocalDate endDate;
}