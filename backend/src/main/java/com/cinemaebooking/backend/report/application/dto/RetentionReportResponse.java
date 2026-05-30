package com.cinemaebooking.backend.report.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RetentionReportResponse {

    private long totalCustomers;
    private long returningCustomers;
    private long oneTimeCustomers;

    private BigDecimal retentionRate;
    private BigDecimal averageBookingsPerCustomer;
}