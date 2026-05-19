package com.cinemaebooking.backend.common.validation.domain;

import com.cinemaebooking.backend.combo.domain.enums.ComboStatus;
import com.cinemaebooking.backend.common.exception.ErrorCategory;
import com.cinemaebooking.backend.common.exception.ErrorDetail;
import com.cinemaebooking.backend.common.validation.builder.StringValidationBuilder;
import com.cinemaebooking.backend.common.validation.builder.ValidationBuilder;
import com.cinemaebooking.backend.common.validation.engine.ValidationRule;
import com.cinemaebooking.backend.common.validation.patterns.ValidationPatterns;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ComboValidationProfile {

    public static final ComboValidationProfile INSTANCE = new ComboValidationProfile();

    private ComboValidationProfile() {}

    // ================== NAME ==================

    public List<ValidationRule<String>> nameRules() {
        return StringValidationBuilder.create()
                .notBlank()
                .length(3, 150)
                .pattern(ValidationPatterns.CINEMA_NAME, "contains invalid characters")
                .containsLetter()
                .build();
    }

    // ================== DESCRIPTION ==================

    public List<ValidationRule<String>> descriptionRules() {
        return ValidationBuilder.<String>create()
                .custom(context -> {
                    String value = context.trimmed();

                    if (value != null && value.length() > 500) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_LENGTH_MAX,
                                        "description must not exceed 500 characters"
                                )
                        );
                    }

                    return Optional.empty();
                })
                .build();
    }

    // ================== PRICE ==================

    public List<ValidationRule<BigDecimal>> priceRules() {
        return ValidationBuilder.<BigDecimal>create()
                .notNull()
                .custom(context -> {
                    BigDecimal value = context.getValue();

                    if (value != null && value.signum() <= 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "price must be greater than 0"
                                )
                        );
                    }

                    return Optional.empty();
                })
                .build();
    }

    // ================== ORIGINAL PRICE ==================

    public List<ValidationRule<BigDecimal>> originalPriceRules() {
        return ValidationBuilder.<BigDecimal>create()
                .notNull()
                .custom(context -> {
                    BigDecimal value = context.getValue();

                    if (value != null && value.signum() <= 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "originalPrice must be greater than 0"
                                )
                        );
                    }

                    return Optional.empty();
                })
                .build();
    }

    // ================== STOCK ==================

    public List<ValidationRule<Integer>> stockRules() {
        return ValidationBuilder.<Integer>create()
                .notNull()
                .custom(context -> {
                    Integer value = context.getValue();

                    if (value != null && value < 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "stock must not be negative"
                                )
                        );
                    }

                    return Optional.empty();
                })
                .build();
    }

    public List<ValidationRule<Integer>> optionalStockRules() {
        return ValidationBuilder.<Integer>create()
                .custom(context -> {
                    Integer value = context.getValue();

                    if (value != null && value < 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "stock must not be negative"
                                )
                        );
                    }

                    return Optional.empty();
                })
                .build();
    }

    // ================== IMAGE URL ==================

    public List<ValidationRule<String>> imageUrlRules() {
        return ValidationBuilder.<String>create()
                .custom(context -> {
                    String value = context.trimmed();

                    if (value != null && value.length() > 500) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_LENGTH_MAX,
                                        "imageUrl must not exceed 500 characters"
                                )
                        );
                    }

                    return Optional.empty();
                })
                .build();
    }

    // ================== STATUS ==================

    public List<ValidationRule<ComboStatus>> statusRules() {
        return ValidationBuilder.<ComboStatus>create()
                .notNull()
                .build();
    }
}
