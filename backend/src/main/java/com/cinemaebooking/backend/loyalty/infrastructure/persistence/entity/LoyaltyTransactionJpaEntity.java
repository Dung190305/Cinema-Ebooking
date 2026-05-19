package com.cinemaebooking.backend.loyalty.infrastructure.persistence.entity;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.coupon.infrastructure.persistence.entity.CouponJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.entity.BaseJpaEntity;
import com.cinemaebooking.backend.loyalty.domain.enums.LoyaltyTransactionType;
import com.cinemaebooking.backend.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.cinemaebooking.backend.user_coupon.infrastructure.persistence.entity.UserCouponJpaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LoyaltyTransactionJpaEntity - Persistence model for loyalty_transactions table.
 * Responsibility:
 * - Map database table loyalty_transactions
 * - Handle persistence concerns only
 * - No business logic allowed
 * Note:
 * - This is NOT a domain model
 * - Must be converted via Mapper
 * - balanceAfter is stored for audit and prevents recalculation issues
 * - This entity must NOT be deleted (no soft delete / no hard delete), it is an audit log
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Entity
@Table(
        name = "loyalty_transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_loyalty_transactions_payment_id_type_deleted",
                        columnNames = {"payment_id", "type", "deleted"}
                ),
                @UniqueConstraint(
                        name = "uk_loyalty_transactions_booking_id_type_deleted",
                        columnNames = {"booking_id", "type", "deleted"}
                ),
                @UniqueConstraint(
                        name = "uk_loyalty_transactions_user_coupon_id_type_deleted",
                        columnNames = {"user_coupon_id", "type", "deleted"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class LoyaltyTransactionJpaEntity extends BaseJpaEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_account_id", nullable = false)
    private LoyaltyAccountJpaEntity loyaltyAccount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoyaltyTransactionType type;

    @Column(name = "change_point", nullable = false, precision = 12, scale = 2)
    private BigDecimal changePoint;

    @PositiveOrZero
    @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private BookingJpaEntity booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentJpaEntity payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private CouponJpaEntity coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id")
    private UserCouponJpaEntity userCoupon;

    @NotNull
    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;
}