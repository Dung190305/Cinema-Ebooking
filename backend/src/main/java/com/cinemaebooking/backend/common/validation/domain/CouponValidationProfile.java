package com.cinemaebooking.backend.common.validation.domain;

import com.cinemaebooking.backend.common.exception.ErrorCategory;
import com.cinemaebooking.backend.common.exception.ErrorDetail;
import com.cinemaebooking.backend.common.validation.builder.StringValidationBuilder;
import com.cinemaebooking.backend.common.validation.builder.ValidationBuilder;
import com.cinemaebooking.backend.common.validation.engine.ValidationRule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CouponValidationProfile {

    public static final CouponValidationProfile INSTANCE =
            new CouponValidationProfile();

    private CouponValidationProfile() {}

    // ================== CODE ==================

    public List<ValidationRule<String>> codeRules() {
        return StringValidationBuilder.create()
                .notBlank()
                .length(3, 100)
                .pattern(
                        "^[A-Z0-9_-]+$",
                        "must contain only uppercase letters, numbers, underscore or hyphen"
                )
                .build();
    }

    // ================== USAGE LIMIT ==================

    public List<ValidationRule<Integer>> usageLimitRules() {
        return ValidationBuilder.<Integer>create()
                .notNull()
                .custom(context -> {
                    Integer value = context.getValue();

                    if (value != null && value <= 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "usageLimit must be greater than 0"
                                )
                        );
                    }

                    return Optional.empty();
                })
                .build();
    }

    // ================== PER USER USAGE ==================

    public List<ValidationRule<Integer>> perUserUsageRules() {
        return ValidationBuilder.<Integer>create()
                .notNull()
                .custom(context -> {
                    Integer value = context.getValue();

                    if (value != null && value <= 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "perUserUsage must be greater than 0"
                                )
                        );
                    }

                    return Optional.empty();
                })
                .build();
    }

    // ================== POINTS TO REDEEM ==================

    public List<ValidationRule<Integer>> pointsToRedeemRules() {
        return ValidationBuilder.<Integer>create()
                .notNull()
                .custom(context -> {
                    Integer value = context.getValue();

                    if (value != null && value < 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "pointsToRedeem must not be negative"
                                )
                        );
                    }

                    return Optional.empty();
                })
                .build();
    }

    // ================== VALUE ==================

    public List<ValidationRule<BigDecimal>> valueRules() {
        return ValidationBuilder.<BigDecimal>create()
                .notNull()
                .custom(context -> {
                    BigDecimal value = context.getValue();

                    if (value != null && value.signum() <= 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "value must be greater than 0"
                                )
                        );
                    }

                    return Optional.empty();
                })
                .build();
    }

    // ================== MINIMUM BOOKING VALUE ==================

    public List<ValidationRule<BigDecimal>> minimumBookingValueRules() {
        return ValidationBuilder.<BigDecimal>create()
                .custom(context -> {
                    BigDecimal value = context.getValue();

                    if (value != null && value.signum() < 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "minimumBookingValue must not be negative"
                                )
                        );
                    }

                    return Optional.empty();
                })
                .build();
    }

    // ================== MAXIMUM DISCOUNT AMOUNT ==================

    public List<ValidationRule<BigDecimal>> maximumDiscountAmountRules() {
        return ValidationBuilder.<BigDecimal>create()
                .custom(context -> {
                    BigDecimal value = context.getValue();

                    if (value != null && value.signum() <= 0) {
                        return Optional.of(
                                new ErrorDetail(
                                        context.getField(),
                                        ErrorCategory.INVALID_VALUE,
                                        "maximumDiscountAmount must be greater than 0"
                                )
                        );
                    }

                    return Optional.empty();
                })
                .build();
    }

    // ================== START DATE ==================

    public List<ValidationRule<LocalDate>> startDateRules() {
        return ValidationBuilder.<LocalDate>create()
                .notNull()
                .build();
    }

    // ================== END DATE ==================

    public List<ValidationRule<LocalDate>> endDateRules() {
        return ValidationBuilder.<LocalDate>create()
                .notNull()
                .build();
    }
}