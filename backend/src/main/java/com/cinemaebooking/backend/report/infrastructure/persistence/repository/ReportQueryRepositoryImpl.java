package com.cinemaebooking.backend.report.infrastructure.persistence.repository;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.report.application.port.ReportQueryPort;
import com.cinemaebooking.backend.payment.domain.enums.PaymentStatus;
import com.cinemaebooking.backend.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.cinemaebooking.backend.showtime.infrastructure.persistence.entity.ShowtimeJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportQueryRepositoryImpl implements ReportQueryPort {

    private final EntityManager entityManager;

    @Override
    public List<BookingJpaEntity> findBookingsByCreatedAt(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long cinemaId,
            Long movieId
    ) {
        String jpql = """
                SELECT DISTINCT b
                FROM BookingJpaEntity b
                LEFT JOIN b.tickets t
                LEFT JOIN t.showtimeSeat ss
                LEFT JOIN ss.showtime st
                LEFT JOIN st.movie m
                LEFT JOIN st.room r
                LEFT JOIN r.cinema c
                WHERE b.deleted = false
                  AND b.createdAt >= :fromDate
                  AND b.createdAt < :toDate
                  AND (:cinemaId IS NULL OR c.id = :cinemaId)
                  AND (:movieId IS NULL OR m.id = :movieId)
                """;

        TypedQuery<BookingJpaEntity> query =
                entityManager.createQuery(jpql, BookingJpaEntity.class);

        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("cinemaId", cinemaId);
        query.setParameter("movieId", movieId);

        return query.getResultList();
    }

    @Override
    public List<BookingJpaEntity> findConfirmedBookingsByPaidAt(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long cinemaId,
            Long movieId
    ) {
        String jpql = """
                SELECT DISTINCT b
                FROM BookingJpaEntity b
                LEFT JOIN b.tickets t
                LEFT JOIN t.showtimeSeat ss
                LEFT JOIN ss.showtime st
                LEFT JOIN st.movie m
                LEFT JOIN st.room r
                LEFT JOIN r.cinema c
                WHERE b.deleted = false
                  AND b.status = :status
                  AND b.paidAt IS NOT NULL
                  AND b.paidAt >= :fromDate
                  AND b.paidAt < :toDate
                  AND (:cinemaId IS NULL OR c.id = :cinemaId)
                  AND (:movieId IS NULL OR m.id = :movieId)
                """;

        TypedQuery<BookingJpaEntity> query =
                entityManager.createQuery(jpql, BookingJpaEntity.class);

        query.setParameter("status", BookingStatus.CONFIRMED);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("cinemaId", cinemaId);
        query.setParameter("movieId", movieId);

        return query.getResultList();
    }

    @Override
    public List<PaymentJpaEntity> findSuccessfulPaymentsByPaidAt(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long cinemaId,
            Long movieId
    ) {
        String jpql = """
            SELECT DISTINCT p
            FROM PaymentJpaEntity p
            JOIN p.booking b
            LEFT JOIN b.tickets t
            LEFT JOIN t.showtimeSeat ss
            LEFT JOIN ss.showtime st
            LEFT JOIN st.movie m
            LEFT JOIN st.room r
            LEFT JOIN r.cinema c
            WHERE p.deleted = false
              AND b.deleted = false
              AND p.status = :status
              AND p.paidAt IS NOT NULL
              AND p.paidAt >= :fromDate
              AND p.paidAt < :toDate
              AND (:cinemaId IS NULL OR c.id = :cinemaId)
              AND (:movieId IS NULL OR m.id = :movieId)
            """;

        TypedQuery<PaymentJpaEntity> query =
                entityManager.createQuery(jpql, PaymentJpaEntity.class);

        query.setParameter("status", PaymentStatus.SUCCESS);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("cinemaId", cinemaId);
        query.setParameter("movieId", movieId);

        return query.getResultList();
    }

    @Override
    public List<ShowtimeJpaEntity> findShowtimesByStartTime(
            Instant fromDate,
            Instant toDate,
            Long cinemaId,
            Long movieId
    ) {
        String jpql = """
            SELECT st
            FROM ShowtimeJpaEntity st
            JOIN st.movie m
            JOIN st.room r
            JOIN r.cinema c
            WHERE st.startTime >= :fromDate
              AND st.startTime < :toDate
              AND (:cinemaId IS NULL OR c.id = :cinemaId)
              AND (:movieId IS NULL OR m.id = :movieId)
            """;

        TypedQuery<ShowtimeJpaEntity> query =
                entityManager.createQuery(jpql, ShowtimeJpaEntity.class);

        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("cinemaId", cinemaId);
        query.setParameter("movieId", movieId);

        return query.getResultList();
    }
}
