package com.cinemaebooking.backend.report.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MoviePerformanceResponse {

    private Long movieId;
    private String movieTitle;

    private long bookingCount;
    private long ticketSold;

    private BigDecimal revenue;
    private BigDecimal revenueShare;
}
