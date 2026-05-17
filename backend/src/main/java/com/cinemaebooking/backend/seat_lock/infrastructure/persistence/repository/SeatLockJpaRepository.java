package com.cinemaebooking.backend.seat_lock.infrastructure.persistence.repository;

import com.cinemaebooking.backend.seat_lock.infrastructure.persistence.entity.SeatLockJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SeatLockJpaRepository - Repository trực tiếp với bảng seat_locks.
 *
 * <p>Chịu trách nhiệm:
 * <ul>
 *   <li>CRUD seat_locks</li>
 *   <li>Query tìm lock theo showtimeSeatId, userId, bookingId</li>
 *   <li>Query tìm lock hết hạn (cho scheduled cleanup)</li>
 * </ul>
 *
 * <p>Lưu ý:
 * <ul>
 *   <li>Chỉ làm việc với JPA entity, không chứa business logic</li>
 *   <li>Seat lock là dữ liệu tạm — dùng hard delete khi hết hạn</li>
 * </ul>
 *
 * @author ducthinhn
 * @since 2026
 */
@Repository
public interface SeatLockJpaRepository extends SoftDeleteJpaRepository<SeatLockJpaEntity> {

    /**
     * Tìm lock active (chưa xóa, chưa hết hạn) theo showtimeSeatId.
     * Dùng để kiểm tra ghế có đang bị lock không.
     */
    @Query("SELECT sl FROM SeatLockJpaEntity sl " +
            "WHERE sl.showtimeSeat.id = :showtimeSeatId " +
            "AND sl.deleted = false " +
            "AND sl.expiredAt > :now")
    Optional<SeatLockJpaEntity> findActiveLockByShowtimeSeatId(Long showtimeSeatId, LocalDateTime now);

    /**
     * Tìm lock active theo showtimeSeatId và userId.
     * Dùng để kiểm tra xem user hiện tại có đang giữ ghế này không.
     */
    @Query("SELECT sl FROM SeatLockJpaEntity sl " +
            "WHERE sl.showtimeSeat.id = :showtimeSeatId " +
            "AND sl.user.id = :userId " +
            "AND sl.deleted = false " +
            "AND sl.expiredAt > :now")
    Optional<SeatLockJpaEntity> findActiveLockByShowtimeSeatIdAndUserId(
            Long showtimeSeatId, Long userId, LocalDateTime now);

    /**
     * Tìm tất cả lock active của một user cho một suất chiếu.
     */
    @Query("SELECT sl FROM SeatLockJpaEntity sl " +
            "WHERE sl.user.id = :userId " +
            "AND sl.showtimeSeat.showtime.id = :showtimeId " +
            "AND sl.deleted = false " +
            "AND sl.expiredAt > :now")
    List<SeatLockJpaEntity> findActiveLocksByUserIdAndShowtimeId(Long userId, Long showtimeId, LocalDateTime now);

    /**
     * Tìm tất cả lock active theo bookingId.
     */
    @Query("SELECT sl FROM SeatLockJpaEntity sl " +
            "WHERE sl.booking.id = :bookingId " +
            "AND sl.deleted = false " +
            "AND sl.expiredAt > :now")
    List<SeatLockJpaEntity> findActiveLocksByBookingId(Long bookingId, LocalDateTime now);

    /**
     * Tìm tất cả lock đã hết hạn (dùng cho scheduled cleanup).
     */
    @Query("SELECT sl FROM SeatLockJpaEntity sl WHERE sl.expiredAt <= :now AND sl.deleted = false")
    List<SeatLockJpaEntity> findExpiredLocks(LocalDateTime now);

    /**
     * Xóa tất cả lock của một user cho một suất chiếu (hard delete).
     */
    @Modifying
    @Query("DELETE FROM SeatLockJpaEntity sl " +
            "WHERE sl.user.id = :userId AND sl.showtimeSeat.showtime.id = :showtimeId " +
            "AND sl.deleted = false")
    void deleteByUserIdAndShowtimeId(Long userId, Long showtimeId);

    /**
     * Xóa tất cả lock của một booking (hard delete).
     */
    @Modifying
    @Query("DELETE FROM SeatLockJpaEntity sl WHERE sl.booking.id = :bookingId AND sl.deleted = false")
    void deleteByBookingId(Long bookingId);

    /**
     * Hard delete một lock cụ thể.
     */
    @Modifying
    @Query("DELETE FROM SeatLockJpaEntity sl WHERE sl.id = :id")
    void hardDeleteById(Long id);
}
