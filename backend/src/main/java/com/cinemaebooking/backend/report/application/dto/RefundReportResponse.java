package com.cinemaebooking.backend.report.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RefundReportResponse {

    private long totalRefunds;

    private long requestedRefunds;

    private long approvedRefunds;

    private long completedRefunds;

    private long rejectedRefunds;

    private long cancelledRefunds;

    private BigDecimal totalOriginalAmount;

    private BigDecimal totalRefundAmount;

    private BigDecimal averageRefundAmount;
}
