package com.cinemaebooking.backend.report.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RevenueOverviewResponse {

    private BigDecimal totalRevenue;
    private BigDecimal grossRevenue;
    private BigDecimal totalTicketRevenue;
    private BigDecimal totalComboRevenue;
    private BigDecimal totalRefundAmount;
    private BigDecimal netRevenue;

    private long totalBookings;
    private long confirmedBookings;
    private long pendingBookings;
    private long cancelledBookings;

    private long totalRefunds;
    private long requestedRefunds;
    private long approvedRefunds;
    private long completedRefunds;
    private long rejectedRefunds;
    private long cancelledRefunds;

    private long totalTicketsSold;

    private BigDecimal averageRevenuePerBooking;
}
