package com.cinemaebooking.backend.booking.infrastructure.persistence.repository;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingJpaRepository extends SoftDeleteJpaRepository<BookingJpaEntity> {

    // 1a. Tìm chi tiết booking (cho GET /{id})
    @EntityGraph(attributePaths = {"tickets", "combos", "coupon"})
    Optional<BookingJpaEntity> findByIdAndDeletedFalse(Long id);

    // 1b. Tìm với pessimistic lock (dùng trong ConfirmPayment)
    @EntityGraph(attributePaths = {"tickets", "combos", "coupon"})
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BookingJpaEntity b where b.id = :id and b.deleted = false")
    Optional<BookingJpaEntity> findByIdForUpdate(@Param("id") Long id);

    // 2. Tìm theo mã code (giữ nguyên logic của Hiếu)
    @EntityGraph(attributePaths = {"user", "tickets", "combos", "coupon"})
    Optional<BookingJpaEntity> findByBookingCodeAndDeletedFalse(String bookingCode);

    // 3. Phân trang danh sách theo User và Status
    Page<BookingJpaEntity> findByUserIdAndStatusAndDeletedFalse(Long userId, BookingStatus status, Pageable pageable);

    // 4. Phân trang danh sách theo User (tất cả status)
    Page<BookingJpaEntity> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    Optional<BookingJpaEntity> findByUserIdAndShowtimeIdAndStatus(Long userId, Long showtimeId, BookingStatus status);

    List<BookingJpaEntity> findAllByShowtimeIdAndStatusAndDeletedFalse(
            Long showtimeId, BookingStatus status);

    // 4.1. Admin: phân trang toàn bộ booking
    Page<BookingJpaEntity> findByDeletedFalse(Pageable pageable);

    // 4.2. Admin: phân trang toàn bộ booking theo status
    Page<BookingJpaEntity> findByStatusAndDeletedFalse(BookingStatus status, Pageable pageable);

    @Query("""
        SELECT DISTINCT b
        FROM BookingJpaEntity b
        LEFT JOIN b.tickets t
        LEFT JOIN t.showtimeSeat ss
        LEFT JOIN ss.showtime st
        LEFT JOIN st.movie m
        WHERE b.deleted = false
          AND (:movieId IS NULL OR m.id = :movieId)
          AND (:status IS NULL OR b.status = :status)
          AND (:fromDate IS NULL OR b.createdAt >= :fromDate)
          AND (:toDate IS NULL OR b.createdAt < :toDate)
        """)
    Page<BookingJpaEntity> searchAdminBookings(
            @Param("movieId") Long movieId,
            @Param("status") BookingStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    // 5. Kiểm tra trùng mã (Dùng cho logic tạo code mới)
    boolean existsByBookingCodeAndDeletedFalse(String bookingCode);

    List<BookingJpaEntity> findAllByExpiredAtBeforeAndStatusAndDeletedFalse(
            LocalDateTime now,
            BookingStatus status
    );

    // ── Admin user detail ──────────────────────────────────────────────────────
    long countByUserIdAndDeletedFalse(Long userId);

    @Query("""
            SELECT COALESCE(SUM(b.finalAmount), 0)
            FROM BookingJpaEntity b
            WHERE b.user.id = :userId
              AND b.deleted = false
              AND b.status = :status
            """)
    BigDecimal sumFinalAmountByUserIdAndStatusPaid(
            @Param("userId") Long userId,
            @Param("status") BookingStatus status);

    // Thay thế cả cụm @Query và hàm cũ bằng dòng này:
    Optional<BookingJpaEntity> findFirstByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    // Dùng cho notification: fetch booking kèm user info (email, fullName)
    @Query("""
            SELECT b FROM BookingJpaEntity b
            JOIN FETCH b.user u
            WHERE b.id = :bookingId AND b.deleted = false
            """)
    Optional<BookingJpaEntity> findByIdWithUser(@Param("bookingId") Long bookingId);

    // Dùng cho email: fetch booking kèm user, tickets, combos, coupon
    @Query("""
            SELECT DISTINCT b FROM BookingJpaEntity b
            JOIN FETCH b.user u
            LEFT JOIN FETCH b.tickets
            LEFT JOIN FETCH b.combos
            LEFT JOIN FETCH b.coupon
            WHERE b.id = :bookingId AND b.deleted = false
            """)
    Optional<BookingJpaEntity> findByIdWithDetails(@Param("bookingId") Long bookingId);

    // Dùng cho review eligibility: chỉ cần booking + tickets để kiểm tra check-in
    @Query("SELECT b FROM BookingJpaEntity b LEFT JOIN FETCH b.tickets WHERE b.id = :bookingId AND b.deleted = false")
    Optional<BookingJpaEntity> findByIdWithTickets(@Param("bookingId") Long bookingId);

    // Dùng cho ReminderNotificationJob: tìm booking CONFIRMED sắp chiếu trong khoảng thời gian
    @Query("""
            SELECT b FROM BookingJpaEntity b
            JOIN FETCH b.user u
            WHERE b.status = :status
              AND b.deleted = false
              AND b.showtimeStartTime > :fromTime
              AND b.showtimeStartTime <= :toTime
            """)
    List<BookingJpaEntity> findBookingsNeedingReminder(
            @Param("status") BookingStatus status,
            @Param("fromTime") Instant fromTime,
            @Param("toTime") Instant toTime
    );

    // ── Review eligibility: kiểm tra vé đã check-in theo user + movie ───────────
    // Trả về danh sách booking (đã check-in) của user cho 1 phim cụ thể
    // Dùng cho GET /reviews/movies/{movieId}/check-ticket
    @Query("""
            SELECT DISTINCT b FROM BookingJpaEntity b
            JOIN b.tickets t
            WHERE b.user.id = :userId
              AND b.movieId = :movieId
              AND b.status = 'CONFIRMED'
              AND t.status = 'USED'
              AND b.deleted = false
            """)
    List<BookingJpaEntity> findCheckedInBookingsByUserAndMovie(
            @Param("userId") Long userId,
            @Param("movieId") Long movieId
    );

    @Query("""
    SELECT DISTINCT b
    FROM BookingJpaEntity b
    LEFT JOIN b.tickets t
    LEFT JOIN t.showtimeSeat ss
    LEFT JOIN ss.showtime st
    LEFT JOIN st.movie m
    WHERE b.deleted = false
      AND b.user.id = :userId
      AND (:movieId IS NULL OR m.id = :movieId)
      AND (:status IS NULL OR b.status = :status)
      AND (:fromDate IS NULL OR b.createdAt >= :fromDate)
      AND (:toDate IS NULL OR b.createdAt < :toDate)
""")
    Page<BookingJpaEntity> searchUserBookings(
            @Param("userId") Long userId,
            @Param("movieId") Long movieId,
            @Param("status") BookingStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
}