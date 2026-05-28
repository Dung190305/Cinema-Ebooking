package com.cinemaebooking.backend.showtime.application.dto.showtime;

import com.cinemaebooking.backend.showtime.domain.enums.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShowtimeRequest {

    private Long movieId;
    private Long roomId;
    private Long formatId;

    private Instant startTime;
    private Instant endTime;

    private Language audioLanguage;
    private Language subtitleLanguage;
}