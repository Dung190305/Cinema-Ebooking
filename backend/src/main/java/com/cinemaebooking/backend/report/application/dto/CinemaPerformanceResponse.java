package com.cinemaebooking.backend.report.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CinemaPerformanceResponse {

    private Long cinemaId;
    private String cinemaName;

    private BigDecimal revenue;
    private long bookingCount;
    private long ticketSold;

    private BigDecimal occupancyRate;
}