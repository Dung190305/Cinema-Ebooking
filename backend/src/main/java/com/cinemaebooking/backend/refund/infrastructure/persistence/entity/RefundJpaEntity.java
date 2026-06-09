package com.cinemaebooking.backend.refund.infrastructure.persistence.entity;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.entity.BaseJpaEntity;
import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * RefundJpaEntity - Persistence model for refunds table.
 * Responsibility:
 * - Map database table refunds
 * - Handle persistence concerns only
 * - No business logic allowed
 * Note:
 * - This is NOT a domain model
 * - Must be converted via Mapper
 * - Transient fields are loaded via JOIN FETCH in repository queries
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Entity
@Table(
        name = "refunds",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_refunds_booking_id_deleted",
                        columnNames = {"booking_id", "deleted"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class RefundJpaEntity extends BaseJpaEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingJpaEntity booking;

    @PositiveOrZero
    @Column(name = "original_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalAmount;

    @PositiveOrZero
    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Min(0)
    @Max(100)
    @Column(name = "refund_percentage", nullable = false)
    private Integer refundPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefundStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(length = 500)
    private String reason;

    @Column(name = "admin_note", length = 500)
    private String adminNote;

    /**
     * Transient fields — populated via JOIN FETCH in repository queries.
     * Not persisted to database.
     */
    @Transient
    private String bookingCode;

    @Transient
    private String movieTitle;

    @Transient
    private String cinemaName;

    @Transient
    private String roomName;

    @Transient
    private Long userId;

    @Transient
    private String showtimeStartTime;
}
