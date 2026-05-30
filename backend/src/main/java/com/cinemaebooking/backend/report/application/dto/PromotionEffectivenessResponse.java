package com.cinemaebooking.backend.report.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PromotionEffectivenessResponse {

    private String couponCode;

    private long usedCount;
    private BigDecimal totalDiscount;
    private BigDecimal revenueGenerated;
    private BigDecimal averageDiscountPerBooking;
}