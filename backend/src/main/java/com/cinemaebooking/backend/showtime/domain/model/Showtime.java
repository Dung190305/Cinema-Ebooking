package com.cinemaebooking.backend.showtime.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.showtime.domain.enums.Language;
import com.cinemaebooking.backend.showtime.domain.enums.ShowtimeStatus;
import com.cinemaebooking.backend.showtime.domain.valueobject.ShowtimeId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@SuperBuilder(toBuilder = true)
public class Showtime extends BaseEntity<ShowtimeId> {

    private Long movieId;
    private Long roomId;
    private Long roomLayoutId;
    private Long formatId;

    private Instant startTime;
    private Instant endTime;

    private Language audioLanguage;
    private Language subtitleLanguage;

    private boolean cancelled;

    // =====================================================
    // BUSINESS METHODS
    // =====================================================

    public void update(Instant start,
                       Instant end) {

        validateStartEnd(start, end);

        this.startTime = start;
        this.endTime = end;
    }

    public void updateLanguage(Language audio, Language subtitle){
        validateLanguages(audio, subtitle);
        this.audioLanguage = audio;
        this.subtitleLanguage = subtitle;
    }

    public ShowtimeStatus getStatus() {
        if (cancelled) return ShowtimeStatus.CANCELLED;

        Instant now = Instant.now();
        if (now.isBefore(startTime))              return ShowtimeStatus.SCHEDULED;
        if (now.isBefore(endTime))                return ShowtimeStatus.ONGOING;
        return ShowtimeStatus.FINISHED;
    }

    public void cancel() {
        if (this.cancelled) {
            throw CommonExceptions.invalidInput("Showtime already cancelled");
        }
        this.cancelled = true;
    }


    public void validateForCreate() {
        validateMovieId(movieId);
        validateRoomId(roomId);
        validateFormatId(formatId);

        validateStartEnd(startTime, endTime);
        validateLanguages(audioLanguage, subtitleLanguage);

        if (roomLayoutId == null || roomLayoutId <= 0) {
            throw CommonExceptions.invalidInput("roomLayoutId must be a positive number");
        }
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateMovieId(Long id) {
        if (id == null || id <= 0) {
            throw CommonExceptions.invalidInput("movieId must be a positive number");
        }
    }

    private void validateRoomId(Long id) {
        if (id == null || id <= 0) {
            throw CommonExceptions.invalidInput("roomId must be a positive number");
        }
    }

    private void validateFormatId(Long id) {
        if (id == null || id <= 0) {
            throw CommonExceptions.invalidInput("formatId must be a positive number");
        }
    }

    private void validateStartEnd(Instant start, Instant end) {
        if (start == null) {
            throw CommonExceptions.invalidInput("startTime cannot be null");
        }
        if (end == null) {
            throw CommonExceptions.invalidInput("endTime cannot be null");
        }
        if (end.isBefore(start)) {
            throw CommonExceptions.invalidInput("endTime must be after startTime");
        }
    }

    private void validateLanguages(Language audio, Language subtitle) {
        if (audio == null) {
            throw CommonExceptions.invalidInput("Audio language cannot be empty");
        }
        if (subtitle == null) {
            throw CommonExceptions.invalidInput("Subtitle language cannot be empty");
        }
    }
}