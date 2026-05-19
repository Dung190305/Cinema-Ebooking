package com.cinemaebooking.backend.loyalty.domain.valueobject;

import com.cinemaebooking.backend.common.domain.BaseId;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.ErrorCategory;
import lombok.Getter;

@Getter
public class LoyaltyTransactionId extends BaseId {

    private LoyaltyTransactionId(Long value) { super(value); }

    public static LoyaltyTransactionId of(Long value) {
        if (value == null || value <= 0) throw new IllegalArgumentException("Loyalty Transaction Id must be positive");
        return new LoyaltyTransactionId(value);
    }

    public static LoyaltyTransactionId ofNullable(Long value) {
        if (value == null) return null;
        return of(value);
    }
}
