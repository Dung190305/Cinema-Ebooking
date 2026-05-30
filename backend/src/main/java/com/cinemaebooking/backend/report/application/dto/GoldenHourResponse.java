package com.cinemaebooking.backend.report.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GoldenHourResponse {

    private String dayOfWeek;
    private int hour;

    private long bookingCount;
    private long ticketSold;
    private BigDecimal revenue;
}