package com.cinemaebooking.backend.dashboard.infrastructure.persistence.repository;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DashboardJpaRepository extends SoftDeleteJpaRepository<BookingJpaEntity> {

    // ── Total Revenue (Ticket + Combo) for TODAY ────────────────────────────────
    @Query("""
        SELECT COALESCE(SUM(b.totalTicketPrice + b.totalComboPrice), 0)
        FROM BookingJpaEntity b
        WHERE b.deleted = false
          AND b.status = 'CONFIRMED'
          AND b.paidAt >= :dayStart
          AND b.paidAt < :dayEnd
          AND (:cinemaId IS NULL OR b.cinemaName IN (
              SELECT c.name FROM com.cinemaebooking.backend.cinema.infrastructure.persistence.entity.CinemaJpaEntity c
              WHERE c.id = :cinemaId AND c.deleted = false
          ))
        """)
    BigDecimal sumTodayRevenue(
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("cinemaId") Long cinemaId
    );

    // ── Total Revenue (Ticket + Combo) for YESTERDAY ───────────────────────────
    @Query("""
        SELECT COALESCE(SUM(b.totalTicketPrice + b.totalComboPrice), 0)
        FROM BookingJpaEntity b
        WHERE b.deleted = false
          AND b.status = 'CONFIRMED'
          AND b.paidAt >= :yesterdayStart
          AND b.paidAt < :yesterdayEnd
          AND (:cinemaId IS NULL OR b.cinemaName IN (
              SELECT c.name FROM com.cinemaebooking.backend.cinema.infrastructure.persistence.entity.CinemaJpaEntity c
              WHERE c.id = :cinemaId AND c.deleted = false
          ))
        """)
    BigDecimal sumYesterdayRevenue(
            @Param("yesterdayStart") LocalDateTime yesterdayStart,
            @Param("yesterdayEnd") LocalDateTime yesterdayEnd,
            @Param("cinemaId") Long cinemaId
    );

    // ── Total Ticket Revenue for TODAY ─────────────────────────────────────────
    @Query("""
        SELECT COALESCE(SUM(b.totalTicketPrice), 0)
        FROM BookingJpaEntity b
        WHERE b.deleted = false
          AND b.status = 'CONFIRMED'
          AND b.paidAt >= :dayStart
          AND b.paidAt < :dayEnd
          AND (:cinemaId IS NULL OR b.cinemaName IN (
              SELECT c.name FROM com.cinemaebooking.backend.cinema.infrastructure.persistence.entity.CinemaJpaEntity c
              WHERE c.id = :cinemaId AND c.deleted = false
          ))
        """)
    BigDecimal sumTodayTicketRevenue(
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("cinemaId") Long cinemaId
    );

    // ── Total F&B Revenue for TODAY ─────────────────────────────────────────────
    @Query("""
        SELECT COALESCE(SUM(b.totalComboPrice), 0)
        FROM BookingJpaEntity b
        WHERE b.deleted = false
          AND b.status = 'CONFIRMED'
          AND b.paidAt >= :dayStart
          AND b.paidAt < :dayEnd
          AND (:cinemaId IS NULL OR b.cinemaName IN (
              SELECT c.name FROM com.cinemaebooking.backend.cinema.infrastructure.persistence.entity.CinemaJpaEntity c
              WHERE c.id = :cinemaId AND c.deleted = false
          ))
        """)
    BigDecimal sumTodayFnbRevenue(
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("cinemaId") Long cinemaId
    );

    // ── Tickets Sold TODAY ─────────────────────────────────────────────────────
    @Query("""
        SELECT COUNT(b)
        FROM BookingJpaEntity b
        WHERE b.deleted = false
          AND b.status = 'CONFIRMED'
          AND b.paidAt >= :dayStart
          AND b.paidAt < :dayEnd
          AND (:cinemaId IS NULL OR b.cinemaName IN (
              SELECT c.name FROM com.cinemaebooking.backend.cinema.infrastructure.persistence.entity.CinemaJpaEntity c
              WHERE c.id = :cinemaId AND c.deleted = false
          ))
        """)
    Long countTodayBookings(
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("cinemaId") Long cinemaId
    );

    // ── Tickets Sold YESTERDAY ─────────────────────────────────────────────────
    @Query("""
        SELECT COUNT(b)
        FROM BookingJpaEntity b
        WHERE b.deleted = false
          AND b.status = 'CONFIRMED'
          AND b.paidAt >= :yesterdayStart
          AND b.paidAt < :yesterdayEnd
          AND (:cinemaId IS NULL OR b.cinemaName IN (
              SELECT c.name FROM com.cinemaebooking.backend.cinema.infrastructure.persistence.entity.CinemaJpaEntity c
              WHERE c.id = :cinemaId AND c.deleted = false
          ))
        """)
    Long countYesterdayBookings(
            @Param("yesterdayStart") LocalDateTime yesterdayStart,
            @Param("yesterdayEnd") LocalDateTime yesterdayEnd,
            @Param("cinemaId") Long cinemaId
    );

    // ── Hourly Revenue for TODAY ────────────────────────────────────────────────
    // CONVERT_TZ converts from UTC (how MySQL stores DATETIME internally) to the
    // configured app timezone. This makes the hour extraction timezone-aware and
    // decoupled from the JDBC connection's serverTimezone setting.
    @Query(value = """
        SELECT HOUR(CONVERT_TZ(b.paid_at, 'UTC', :toTz)) as hourSlot,
               SUM(b.total_ticket_price + b.total_combo_price) as revenue
        FROM bookings b
        LEFT JOIN cinemas c ON c.name = b.cinema_name AND c.deleted = false
        WHERE b.deleted = false
          AND b.status = 'CONFIRMED'
          AND b.paid_at >= :dayStart
          AND b.paid_at < :dayEnd
          AND (:cinemaId IS NULL OR c.id = :cinemaId)
        GROUP BY HOUR(CONVERT_TZ(b.paid_at, 'UTC', :toTz))
        ORDER BY hourSlot
        """, nativeQuery = true)
    List<Object[]> sumHourlyRevenue(
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("cinemaId") Long cinemaId,
            @Param("toTz") String toTz
    );

    // ── Top 5 Movies by Ticket Count ───────────────────────────────────────────
    @Query("""
        SELECT b.movieTitle,
               COUNT(b) as ticketCount,
               SUM(b.totalTicketPrice + b.totalComboPrice) as revenue
        FROM BookingJpaEntity b
        WHERE b.deleted = false
          AND b.status = 'CONFIRMED'
          AND b.paidAt >= :dayStart
          AND b.paidAt < :dayEnd
          AND (:cinemaId IS NULL OR b.cinemaName IN (
              SELECT c.name FROM com.cinemaebooking.backend.cinema.infrastructure.persistence.entity.CinemaJpaEntity c
              WHERE c.id = :cinemaId AND c.deleted = false
          ))
        GROUP BY b.movieTitle
        ORDER BY ticketCount DESC
        """)
    List<Object[]> findTop5Movies(
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("cinemaId") Long cinemaId
    );

    // ── Occupancy Rate - Total Booked Seats TODAY ──────────────────────────────
    @Query("""
        SELECT COUNT(t)
        FROM com.cinemaebooking.backend.ticket.infrastructure.persistence.entity.TicketJpaEntity t
        JOIN t.booking b
        WHERE t.deletedAt IS NULL
          AND b.deleted = false
          AND b.status = 'CONFIRMED'
          AND b.paidAt >= :dayStart
          AND b.paidAt < :dayEnd
          AND (:cinemaId IS NULL OR b.cinemaName IN (
              SELECT c.name FROM com.cinemaebooking.backend.cinema.infrastructure.persistence.entity.CinemaJpaEntity c
              WHERE c.id = :cinemaId AND c.deleted = false
          ))
        """)
    Long countTodayBookedSeats(
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("cinemaId") Long cinemaId
    );

    // ── Occupancy Rate - Total Booked Seats YESTERDAY ──────────────────────────
    @Query("""
        SELECT COUNT(t)
        FROM com.cinemaebooking.backend.ticket.infrastructure.persistence.entity.TicketJpaEntity t
        JOIN t.booking b
        WHERE t.deletedAt IS NULL
          AND b.deleted = false
          AND b.status = 'CONFIRMED'
          AND b.paidAt >= :yesterdayStart
          AND b.paidAt < :yesterdayEnd
          AND (:cinemaId IS NULL OR b.cinemaName IN (
              SELECT c.name FROM com.cinemaebooking.backend.cinema.infrastructure.persistence.entity.CinemaJpaEntity c
              WHERE c.id = :cinemaId AND c.deleted = false
          ))
        """)
    Long countYesterdayBookedSeats(
            @Param("yesterdayStart") LocalDateTime yesterdayStart,
            @Param("yesterdayEnd") LocalDateTime yesterdayEnd,
            @Param("cinemaId") Long cinemaId
    );

    // ── Total Available Capacity Seats for TODAY's Showtimes ───────────────────
    @Query("""
        SELECT COUNT(ss)
        FROM com.cinemaebooking.backend.showtime_seat.infrastructure.persistence.entity.ShowtimeSeatJpaEntity ss
        JOIN ss.showtime s
        JOIN com.cinemaebooking.backend.room.infrastructure.persistence.entity.RoomJpaEntity r WITH r.id = s.room.id
        WHERE s.deleted = false
          AND ss.active = true
          AND DATE(s.startTime) = CURRENT_DATE
          AND (:cinemaId IS NULL OR r.cinema.id = :cinemaId)
        """)
    Long countTotalAvailableSeats(
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("cinemaId") Long cinemaId
    );

    // ── Total Available Capacity Seats for YESTERDAY's Showtimes ─────────────────
    @Query("""
        SELECT COUNT(ss)
        FROM com.cinemaebooking.backend.showtime_seat.infrastructure.persistence.entity.ShowtimeSeatJpaEntity ss
        JOIN ss.showtime s
        JOIN com.cinemaebooking.backend.room.infrastructure.persistence.entity.RoomJpaEntity r WITH r.id = s.room.id
        WHERE s.deleted = false
          AND ss.active = true
          AND DATE(s.startTime) = DATE(:yesterdayDate)
          AND (:cinemaId IS NULL OR r.cinema.id = :cinemaId)
        """)
    Long countYesterdayAvailableSeats(
            @Param("yesterdayDate") LocalDateTime yesterdayDate,
            @Param("cinemaId") Long cinemaId
    );

    // ── New Users TODAY ────────────────────────────────────────────────────────
    @Query("""
        SELECT COUNT(u)
        FROM com.cinemaebooking.backend.user.infrastructure.persistence.entity.UserJpaEntity u
        WHERE u.deleted = false
          AND u.createdAt >= :dayStart
          AND u.createdAt < :dayEnd
        """)
    Long countNewUsersToday(
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd
    );

    // ── New Users YESTERDAY ─────────────────────────────────────────────────────
    @Query("""
        SELECT COUNT(u)
        FROM com.cinemaebooking.backend.user.infrastructure.persistence.entity.UserJpaEntity u
        WHERE u.deleted = false
          AND u.createdAt >= :yesterdayStart
          AND u.createdAt < :yesterdayEnd
        """)
    Long countNewUsersYesterday(
            @Param("yesterdayStart") LocalDateTime yesterdayStart,
            @Param("yesterdayEnd") LocalDateTime yesterdayEnd
    );
}
