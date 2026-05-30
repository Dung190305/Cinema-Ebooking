package com.cinemaebooking.backend.common.validation.domain;

import com.cinemaebooking.backend.common.validation.builder.StringValidationBuilder;
import com.cinemaebooking.backend.common.validation.builder.ValidationBuilder;
import com.cinemaebooking.backend.common.validation.engine.ValidationRule;

import java.util.List;

public class RefundValidationProfile {

    public static final RefundValidationProfile INSTANCE = new RefundValidationProfile();

    private RefundValidationProfile() {}

    public List<ValidationRule<Long>> bookingIdRules() {
        return ValidationBuilder.<Long>create()
                .notNull()
                .build();
    }

    public List<ValidationRule<String>> reasonRules() {
        return StringValidationBuilder.create()
                .length(0, 500)
                .build();
    }

    public List<ValidationRule<String>> adminNoteRules() {
        return StringValidationBuilder.create()
                .length(0, 500)
                .build();
    }
}
