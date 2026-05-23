package com.cinemaebooking.backend.showtime.application.dto.showtime;

import com.cinemaebooking.backend.showtime.domain.enums.Language;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Builder
public class ShowtimeResponse {
    private Long id;
    private Long movieId;
    private Long roomId;
    private Long formatId;
    private Long cinemaId;
    private Long roomLayoutId;
    private Instant startTime;
    private Instant endTime;
    private Language audioLanguage;
    private Language subtitleLanguage;
    private String status;
}