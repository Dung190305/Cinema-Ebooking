package com.cinemaebooking.backend.seat_lock.infrastructure.persistence.entity;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.entity.BaseJpaEntity;
import com.cinemaebooking.backend.showtime_seat.infrastructure.persistence.entity.ShowtimeSeatJpaEntity;
import com.cinemaebooking.backend.user.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * SeatLockJpaEntity - Persistence model for seat_locks table.
 *
 * <p>Đại diện một seat lock tạm thời khi user chọn ghế trên UI.
 * Sau khi booking được xác nhận, lock bị xóa (hard delete).
 *
 * <p>Lưu ý:
 * <ul>
 *   <li>booking có thể null — lock được tạo TRƯỚC khi booking được tạo</li>
 *   <li>Khi booking được tạo, bookingId có thể được update vào lock</li>
 *   <li>Lock là dữ liệu tạm → dùng hard delete khi hết hạn hoặc booking confirmed</li>
 * </ul>
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Entity
@Table(
        name = "seat_locks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seat_locks_showtime_seat_id_deleted_at",
                        columnNames = {"showtime_seat_id", "deleted_at"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SeatLockJpaEntity extends BaseJpaEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_seat_id", nullable = false)
    private ShowtimeSeatJpaEntity showtimeSeat;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    /**
     * Có thể null — lock được tạo trước khi booking tồn tại.
     * Được set khi booking được tạo từ lock.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = true)
    private BookingJpaEntity booking;

    @NotNull
    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;

    @NotNull
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
}
