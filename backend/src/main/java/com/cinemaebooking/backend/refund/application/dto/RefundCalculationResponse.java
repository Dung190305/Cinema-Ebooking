package com.cinemaebooking.backend.refund.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RefundCalculationResponse {

    private Long bookingId;

    private BigDecimal originalAmount;

    private BigDecimal refundAmount;

    private Integer refundPercentage;

    private String message;
}
