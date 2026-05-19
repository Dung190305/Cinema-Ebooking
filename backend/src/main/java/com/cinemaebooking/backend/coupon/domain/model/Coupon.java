package com.cinemaebooking.backend.coupon.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.common.exception.ErrorCategory;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.CouponExceptions;
import com.cinemaebooking.backend.coupon.domain.enums.CouponDisplayStatus;
import com.cinemaebooking.backend.coupon.domain.enums.CouponStatus;
import com.cinemaebooking.backend.coupon.domain.enums.CouponType;
import com.cinemaebooking.backend.coupon.domain.valueobject.CouponId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@SuperBuilder
public class Coupon extends BaseEntity<CouponId> {

    private String code;
    private CouponType type;
    private BigDecimal value;
    private Integer usageLimit;
    private Integer remainingUsage;
    private Integer perUserUsage;
    private Integer pointsToRedeem;
    private BigDecimal minimumBookingValue;
    private BigDecimal maximumDiscountAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private CouponStatus status;

    // ================== BUSINESS METHODS ==================

    public void update(
                       Integer usageLimit,
                       LocalDate endDate
                       ) {
        if (usageLimit < this.usageLimit - this.remainingUsage) {
            throw CommonExceptions.invalidInput("usageLimit", ErrorCategory.INVALID_VALUE,"Giới hạn sử dụng không thể nhỏ hơn số lượt đã sử dụng");
        }else if (usageLimit < this.usageLimit) {
            this.remainingUsage -= this.usageLimit - usageLimit;
        }else {
            this.remainingUsage += usageLimit - this.usageLimit;
        }
        this.usageLimit = usageLimit;
        this.endDate = endDate;
    }

    public void updateDraft(
                            String code,
                            CouponType type,
                            BigDecimal value,
                            Integer usageLimit,
                            Integer perUserUsage,
                            Integer pointsToRedeem,
                            BigDecimal minimumBookingValue,
                            BigDecimal maximumDiscountAmount,
                            LocalDate startDate,
                            LocalDate endDate
                            ){
        this.code = code;
        this.type = type;
        this.value = value;
        this.usageLimit = usageLimit;
        this.remainingUsage = usageLimit;
        this.perUserUsage = perUserUsage;
        this.pointsToRedeem = pointsToRedeem;
        this.minimumBookingValue = minimumBookingValue;
        this.maximumDiscountAmount = maximumDiscountAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return !startDate.isAfter(now) && !endDate.isBefore(now);
    }

    public void activate() {

        if (this.status == CouponStatus.ACTIVE) {
            return;
        }

        if (this.remainingUsage <= 0) {
            throw new IllegalStateException(
                    "Cannot activate coupon with no remaining usage"
            );
        }

        if (this.endDate.isBefore(LocalDate.now())) {
            throw new IllegalStateException(
                    "Cannot activate expired coupon"
            );
        }

        this.status = CouponStatus.ACTIVE;
    }

    public void disable() {

        if (this.status == CouponStatus.DISABLED) {
            return;
        }

        if (this.endDate.isBefore(LocalDate.now())) {
            throw CouponExceptions.expired(
                    "Coupon đã hết hạn và không thể disable"
            );
        }

        this.status = CouponStatus.DISABLED;
    }

    public CouponDisplayStatus getEffectiveStatus() {

        if (this.status == CouponStatus.DISABLED) {
            return CouponDisplayStatus.DISABLED;
        }

        if (this.remainingUsage <= 0) {
            return CouponDisplayStatus.OUT_OF_STOCK;
        }

        if (this.endDate.isBefore(LocalDate.now())) {
            return CouponDisplayStatus.EXPIRED;
        }

        return switch (this.status) {
            case DRAFT -> CouponDisplayStatus.DRAFT;
            case ACTIVE -> CouponDisplayStatus.ACTIVE;
            case DISABLED -> CouponDisplayStatus.DISABLED;
        };
    }


    public boolean isExpired() {
        return endDate.isBefore(LocalDate.now());
    }

    public boolean isWithinValidityPeriod() {
        return !startDate.isAfter(LocalDate.now()) && !endDate.isBefore(LocalDate.now());
    }

}