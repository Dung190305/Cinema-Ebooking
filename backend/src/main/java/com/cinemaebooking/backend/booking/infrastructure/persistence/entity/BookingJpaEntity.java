package com.cinemaebooking.backend.booking.infrastructure.persistence.entity;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking_combo.infrastructure.persistence.entity.BookingComboJpaEntity;
import com.cinemaebooking.backend.booking_coupon.infrastructure.persistence.entity.BookingCouponJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.entity.BaseJpaEntity;
import com.cinemaebooking.backend.ticket.infrastructure.persistence.entity.TicketJpaEntity;
import com.cinemaebooking.backend.user.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingJpaEntity - Persistence model for bookings table.
 * Responsibility:
 * - Map database table bookings
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
        name = "bookings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bookings_booking_code_deleted",
                        columnNames = {"booking_code", "deleted"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class BookingJpaEntity extends BaseJpaEntity {

    @Column(name = "booking_code", nullable = false, length = 100)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    @Column(name = "movie_title", nullable = false)
    private String movieTitle;

    @Column(name = "cinema_name", nullable = false)
    private String cinemaName;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(name = "showtime_start_time", nullable = false)
    private LocalDateTime showtimeStartTime;

    @Positive
    @Column(name = "total_ticket_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalTicketPrice;

    @PositiveOrZero
    @Column(name = "total_combo_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalComboPrice;

    @PositiveOrZero
    @Column(name = "tier_discount_amount", precision = 12, scale = 2)
    private BigDecimal tierDiscountAmount;

    @PositiveOrZero
    @Column(name = "coupon_discount_amount", precision = 12, scale = 2)
    private BigDecimal couponDiscountAmount;

    @Column(name = "membership_tier_name", length = 50)
    private String membershipTierName;

    @Column(name = "membership_discount_percent", precision = 5, scale = 2)
    private BigDecimal membershipDiscountPercent;

    @Positive
    @Column(name = "final_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketJpaEntity> tickets = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingComboJpaEntity> combos = new ArrayList<>();

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private BookingCouponJpaEntity coupon;

    public void addTicket(TicketJpaEntity ticket) {
        tickets.add(ticket);
        ticket.setBooking(this);
    }

    public void addCombo(BookingComboJpaEntity combo) {
        combos.add(combo);
        combo.setBooking(this);
    }

    public void setCoupon(BookingCouponJpaEntity coupon) {
        this.coupon = coupon;

        if (coupon != null) {
            coupon.setBooking(this);
        }
    }

    @Override
    protected void beforeSoftDelete() {
        this.bookingCode = markDeleted(this.bookingCode);
    }
}