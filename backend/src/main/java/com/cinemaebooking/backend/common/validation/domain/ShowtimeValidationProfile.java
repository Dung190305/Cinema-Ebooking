package com.cinemaebooking.backend.common.validation.domain;

import com.cinemaebooking.backend.common.exception.ErrorCategory;
import com.cinemaebooking.backend.common.exception.ErrorDetail;
import com.cinemaebooking.backend.common.validation.builder.ValidationBuilder;
import com.cinemaebooking.backend.common.validation.engine.ValidationRule;
import com.cinemaebooking.backend.showtime.domain.enums.Language;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ShowtimeValidationProfile {

    public static final ShowtimeValidationProfile INSTANCE =
            new ShowtimeValidationProfile();

    private ShowtimeValidationProfile() {}

    // ================== ROOM ID ==================

    public List<ValidationRule<Long>> roomIdRules() {
        return ValidationBuilder.<Long>create()
                .notNull()
                .custom(context -> {
                    Long value = context.getValue();
                    if (value != null && value <= 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "roomId must be positive"
                                )
                        );
                    }
                    return Optional.empty();
                })
                .build();
    }

    // ================== MOVIE ID ==================

    public List<ValidationRule<Long>> movieIdRules() {
        return ValidationBuilder.<Long>create()
                .notNull()
                .custom(context -> {
                    Long value = context.getValue();
                    if (value != null && value <= 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "movieId must be positive"
                                )
                        );
                    }
                    return Optional.empty();
                })
                .build();
    }
    // ================== FORMAT ID ==================

    public List<ValidationRule<Long>> formatIdRules() {
        return ValidationBuilder.<Long>create()
                .notNull()
                .custom(context -> {
                    Long value = context.getValue();
                    if (value != null && value <= 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "formatId must be positive"
                                )
                        );
                    }
                    return Optional.empty();
                })
                .build();
    }

    // ================== START TIME ==================

    public List<ValidationRule<Instant>> startTimeRules() {
        return ValidationBuilder.<Instant>create()
                .notNull()
                .build();
    }

    public List<ValidationRule<Instant>> endTimeRules() {
        return ValidationBuilder.<Instant>create()
                .notNull()
                .build();
    }

    public List<ValidationRule<Language>> audioLanguageRules() {
        return ValidationBuilder.<Language>create()
                .notNull()
                .build();
    }

    public List<ValidationRule<Language>> subtitleLanguageRules() {
        return ValidationBuilder.<Language>create()
                .notNull()
                .build();
    }
}