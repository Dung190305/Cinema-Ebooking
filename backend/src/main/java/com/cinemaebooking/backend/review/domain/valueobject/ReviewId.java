package com.cinemaebooking.backend.review.domain.valueobject;

import com.cinemaebooking.backend.common.domain.BaseId;

public class ReviewId extends BaseId {
    private ReviewId(Long value) { super(value); }

    public static ReviewId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReviewId must be a positive number");
        }
        return new ReviewId(value);
    }

    public static ReviewId ofNullable(Long value) {
        return value == null ? null : new ReviewId(value);
    }
}
