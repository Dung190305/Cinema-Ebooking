package com.cinemaebooking.backend.dashboard.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingShowtimeDto {
    private Long showtimeId;
    private String movieTitle;
    private String screenRoom;
    private String cinemaBranch;
    private LocalDateTime startTime;
    private Integer bookedSeats;
    private Integer totalSeats;
    private String bookedRatio;
}
