package com.cinemaebooking.backend.report.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RevenueOverviewResponse {

    private BigDecimal totalRevenue;
    private BigDecimal totalTicketRevenue;
    private BigDecimal totalComboRevenue;

    private long totalBookings;
    private long confirmedBookings;
    private long pendingBookings;
    private long cancelledBookings;

    private long totalTicketsSold;

    private BigDecimal averageRevenuePerBooking;
}
