package com.cinemaebooking.backend.ticket.infrastructure.persistence.entity;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.entity.BaseJpaEntity;
import com.cinemaebooking.backend.showtime_seat.infrastructure.persistence.entity.ShowtimeSeatJpaEntity;
import com.cinemaebooking.backend.ticket.domain.enums.TicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * TicketJpaEntity - Persistence model for tickets table.
 * Responsibility:
 * - Map database table tickets
 * - Handle persistence concerns only
 * - No business logic allowed
 * Note:
 * - This is NOT a domain model
 * - Must be converted via Mapper
 * - ticketCode is generated automatically when creating a ticket
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Entity
@Table(
        name = "tickets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tickets_active_showtime_seat",
                        columnNames = {"active_seat_key"}
                ),
                @UniqueConstraint(
                        name = "uk_tickets_ticket_code_deleted_at",
                        columnNames = {"ticket_code", "deleted_at"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TicketJpaEntity extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingJpaEntity booking;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_seat_id", nullable = false)
    private ShowtimeSeatJpaEntity showtimeSeat;

    @Column(name="ticket_code", nullable = false, length = 100)
    private String ticketCode;

    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "seat_type", nullable = false, length = 30)
    private String seatType;

    @Column(name = "seat_name", nullable = false)
    private String seatName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "active_seat_key", insertable = false, updatable = false)
    private String activeSeatKey;

    @Override
    protected void beforeSoftDelete() {
        this.ticketCode = markDeleted(this.ticketCode);
    }
}