package com.cinemaebooking.backend.loyalty.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.loyalty.domain.enums.LoyaltyTransactionType;
import com.cinemaebooking.backend.loyalty.domain.valueobject.LoyaltyTransactionId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@SuperBuilder
public class LoyaltyTransaction extends BaseEntity<LoyaltyTransactionId> {

    private final Long loyaltyAccountId;
    private final LoyaltyTransactionType type;
    private final BigDecimal changePoint;
    private final BigDecimal balanceAfter;
    private final Long bookingId;
    private final Long paymentId;
    private final Long couponId;
    private final Long userCouponId;
    private final LocalDateTime changeDate;

    private LoyaltyTransaction(LoyaltyTransactionBuilder<?, ?> builder) {
        super(builder);
        this.loyaltyAccountId = builder.loyaltyAccountId;
        this.type = builder.type;
        this.changePoint = builder.changePoint;
        this.balanceAfter = builder.balanceAfter;
        this.bookingId = builder.bookingId;
        this.paymentId = builder.paymentId;
        this.couponId = builder.couponId;
        this.userCouponId = builder.userCouponId;
        this.changeDate = builder.changeDate;
    }

    public static LoyaltyTransactionBuilder<?, ?> builder() {
        return new LoyaltyTransactionBuilderImpl();
    }
}
