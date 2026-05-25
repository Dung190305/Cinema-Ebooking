package com.cinemaebooking.backend.report.application.dto;

import com.cinemaebooking.backend.payment.domain.enums.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentMethodReportResponse {

    private PaymentMethod method;

    private long transactionCount;
    private BigDecimal totalAmount;
    private BigDecimal percentage;
}