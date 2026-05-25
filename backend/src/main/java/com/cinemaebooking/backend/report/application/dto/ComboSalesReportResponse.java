package com.cinemaebooking.backend.report.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ComboSalesReportResponse {

    private Long comboId;
    private String comboName;

    private long quantitySold;
    private BigDecimal totalRevenue;
}