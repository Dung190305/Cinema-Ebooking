package com.cinemaebooking.backend.report.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RevenueTrendPointResponse {

    private String label;

    private BigDecimal revenue;
    private BigDecimal ticketRevenue;
    private BigDecimal comboRevenue;

    private long bookingCount;
    private long ticketCount;
}