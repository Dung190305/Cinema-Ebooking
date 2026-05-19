package com.cinemaebooking.backend.coupon.infrastructure.persistence.entity;

import com.cinemaebooking.backend.coupon.domain.enums.CouponStatus;
import com.cinemaebooking.backend.coupon.domain.enums.CouponType;
import com.cinemaebooking.backend.infrastructure.persistence.entity.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CouponJpaEntity - Persistence model for coupons table.
 * Responsibility:
 * - Map database table coupons
 * - Handle persistence concerns only
 * - No business logic allowed
 * Note:
 * - This is NOT a domain model
 * - Must be converted via Mapper
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Entity
@Table(
        name = "coupons",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_coupons_code_deleted",
                        columnNames = {"code", "deleted"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CouponJpaEntity extends BaseJpaEntity {

    @Column(nullable = false, length = 100)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponType type;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit;

    @Column(name = "remaining_usage", nullable = false)
    private Integer remainingUsage;

    @Column(name = "per_user_usage", nullable = false)
    private Integer perUserUsage;

    @Column(name = "points_to_redeem", nullable = false)
    private Integer pointsToRedeem;

    @Column(name = "minimum_booking_value", precision = 12, scale = 2)
    private BigDecimal minimumBookingValue;

    @Column(name = "maximum_discount_amount", precision = 12, scale = 2)
    private BigDecimal maximumDiscountAmount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CouponStatus status;

    @Override
    protected void beforeSoftDelete() {
        this.code = markDeleted(this.code);
    }
}