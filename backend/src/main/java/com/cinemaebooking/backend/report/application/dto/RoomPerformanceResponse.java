package com.cinemaebooking.backend.report.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RoomPerformanceResponse {

    private Long roomId;
    private String roomName;
    private String cinemaName;

    private BigDecimal revenue;
    private long bookingCount;
    private long ticketSold;
    private long showtimeCount;

    private BigDecimal occupancyRate;
}