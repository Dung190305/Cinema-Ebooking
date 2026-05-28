package com.cinemaebooking.backend.booking.infrastructure.persistence.repository;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingJpaRepository extends SoftDeleteJpaRepository<BookingJpaEntity> {

    // 1. Dùng EntityGraph để fetch "tất tần tật" data trong 1 câu query (Tránh N+1)
    // Khi xem chi tiết, bạn cần cả Tickets, Combos và Coupon.
    @EntityGraph(attributePaths = {"tickets", "coupon"})
    Optional<BookingJpaEntity> findWithDetailsById(Long id);

    // 2. Tìm theo mã code (giữ nguyên logic của Hiếu)
    Optional<BookingJpaEntity> findByBookingCodeAndDeletedFalse(String bookingCode);

    // 3. Phân trang danh sách theo User và Status
    Page<BookingJpaEntity> findByUserIdAndStatusAndDeletedFalse(Long userId, BookingStatus status, Pageable pageable);

    // 4. Phân trang danh sách theo User (tất cả status)
    Page<BookingJpaEntity> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    Optional<BookingJpaEntity> findByUserIdAndShowtimeIdAndStatus(Long userId, Long showtimeId, BookingStatus status);

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
}