package com.cinemaebooking.backend.report.infrastructure.persistence.repository;

import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.payment.domain.enums.PaymentStatus;
import com.cinemaebooking.backend.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.cinemaebooking.backend.refund.infrastructure.persistence.entity.RefundJpaEntity;
import com.cinemaebooking.backend.report.application.port.ReportQueryPort;
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
        String cinemaName = findCinemaNameById(cinemaId);
        String movieTitle = findMovieTitleById(movieId);

        if (isInvalidFilter(cinemaId, cinemaName, movieId, movieTitle)) {
            return List.of();
        }

        String jpql = """
                SELECT DISTINCT b
                FROM BookingJpaEntity b
                WHERE b.deleted = false
                  AND b.createdAt >= :fromDate
                  AND b.createdAt < :toDate
                  AND (:cinemaName IS NULL OR b.cinemaName = :cinemaName)
                  AND (:movieTitle IS NULL OR b.movieTitle = :movieTitle)
                """;

        TypedQuery<BookingJpaEntity> query =
                entityManager.createQuery(jpql, BookingJpaEntity.class);

        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("cinemaName", cinemaName);
        query.setParameter("movieTitle", movieTitle);

        return query.getResultList();
    }

    @Override
    public List<BookingJpaEntity> findConfirmedBookingsByPaidAt(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long cinemaId,
            Long movieId
    ) {
        String cinemaName = findCinemaNameById(cinemaId);
        String movieTitle = findMovieTitleById(movieId);

        if (isInvalidFilter(cinemaId, cinemaName, movieId, movieTitle)) {
            return List.of();
        }

        String jpql = """
                SELECT DISTINCT b
                FROM BookingJpaEntity b
                WHERE b.deleted = false
                  AND b.status = :status
                  AND b.paidAt IS NOT NULL
                  AND b.paidAt >= :fromDate
                  AND b.paidAt < :toDate
                  AND (:cinemaName IS NULL OR b.cinemaName = :cinemaName)
                  AND (:movieTitle IS NULL OR b.movieTitle = :movieTitle)
                """;

        TypedQuery<BookingJpaEntity> query =
                entityManager.createQuery(jpql, BookingJpaEntity.class);

        query.setParameter("status", BookingStatus.CONFIRMED);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("cinemaName", cinemaName);
        query.setParameter("movieTitle", movieTitle);

        return query.getResultList();
    }

    @Override
    public List<BookingJpaEntity> findConfirmedBookingsByShowtimeStartTime(
            Instant fromDate,
            Instant toDate,
            Long cinemaId,
            Long movieId
    ) {
        String cinemaName = findCinemaNameById(cinemaId);
        String movieTitle = findMovieTitleById(movieId);

        if (isInvalidFilter(cinemaId, cinemaName, movieId, movieTitle)) {
            return List.of();
        }

        String jpql = """
                SELECT DISTINCT b
                FROM BookingJpaEntity b
                WHERE b.deleted = false
                  AND b.status = :status
                  AND b.showtimeStartTime >= :fromDate
                  AND b.showtimeStartTime < :toDate
                  AND (:cinemaName IS NULL OR b.cinemaName = :cinemaName)
                  AND (:movieTitle IS NULL OR b.movieTitle = :movieTitle)
                """;

        TypedQuery<BookingJpaEntity> query =
                entityManager.createQuery(jpql, BookingJpaEntity.class);

        query.setParameter("status", BookingStatus.CONFIRMED);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("cinemaName", cinemaName);
        query.setParameter("movieTitle", movieTitle);

        return query.getResultList();
    }

    @Override
    public List<PaymentJpaEntity> findSuccessfulPaymentsByPaidAt(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long cinemaId,
            Long movieId
    ) {
        String cinemaName = findCinemaNameById(cinemaId);
        String movieTitle = findMovieTitleById(movieId);

        if (isInvalidFilter(cinemaId, cinemaName, movieId, movieTitle)) {
            return List.of();
        }

        String jpql = """
                SELECT DISTINCT p
                FROM PaymentJpaEntity p
                JOIN p.booking b
                WHERE p.deleted = false
                  AND b.deleted = false
                  AND b.status = :bookingStatus
                  AND p.status = :paymentStatus
                  AND p.paidAt IS NOT NULL
                  AND p.paidAt >= :fromDate
                  AND p.paidAt < :toDate
                  AND (:cinemaName IS NULL OR b.cinemaName = :cinemaName)
                  AND (:movieTitle IS NULL OR b.movieTitle = :movieTitle)
                """;

        TypedQuery<PaymentJpaEntity> query =
                entityManager.createQuery(jpql, PaymentJpaEntity.class);

        query.setParameter("bookingStatus", BookingStatus.CONFIRMED);
        query.setParameter("paymentStatus", PaymentStatus.SUCCESS);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("cinemaName", cinemaName);
        query.setParameter("movieTitle", movieTitle);

        return query.getResultList();
    }

    @Override
    public List<RefundJpaEntity> findRefundsByProcessedAt(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long cinemaId,
            Long movieId
    ) {
        String cinemaName = findCinemaNameById(cinemaId);
        String movieTitle = findMovieTitleById(movieId);

        if (isInvalidFilter(cinemaId, cinemaName, movieId, movieTitle)) {
            return List.of();
        }

        String jpql = """
                SELECT DISTINCT rf
                FROM RefundJpaEntity rf
                JOIN rf.booking b
                WHERE rf.deleted = false
                  AND rf.processedAt IS NOT NULL
                  AND rf.processedAt >= :fromDate
                  AND rf.processedAt < :toDate
                  AND (:cinemaName IS NULL OR b.cinemaName = :cinemaName)
                  AND (:movieTitle IS NULL OR b.movieTitle = :movieTitle)
                """;

        TypedQuery<RefundJpaEntity> query =
                entityManager.createQuery(jpql, RefundJpaEntity.class);

        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("cinemaName", cinemaName);
        query.setParameter("movieTitle", movieTitle);

        return query.getResultList();
    }

    @Override
    public List<RefundJpaEntity> findRefundsByRequestedAt(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long cinemaId,
            Long movieId
    ) {
        String cinemaName = findCinemaNameById(cinemaId);
        String movieTitle = findMovieTitleById(movieId);

        if (isInvalidFilter(cinemaId, cinemaName, movieId, movieTitle)) {
            return List.of();
        }

        String jpql = """
                SELECT DISTINCT rf
                FROM RefundJpaEntity rf
                JOIN rf.booking b
                WHERE rf.deleted = false
                  AND rf.requestedAt >= :fromDate
                  AND rf.requestedAt < :toDate
                  AND (:cinemaName IS NULL OR b.cinemaName = :cinemaName)
                  AND (:movieTitle IS NULL OR b.movieTitle = :movieTitle)
                """;

        TypedQuery<RefundJpaEntity> query =
                entityManager.createQuery(jpql, RefundJpaEntity.class);

        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("cinemaName", cinemaName);
        query.setParameter("movieTitle", movieTitle);

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

    private String findMovieTitleById(Long movieId) {
        if (movieId == null) {
            return null;
        }

        String jpql = """
                SELECT m.title
                FROM MovieJpaEntity m
                WHERE m.id = :movieId
                  AND m.deleted = false
                """;

        List<String> titles = entityManager
                .createQuery(jpql, String.class)
                .setParameter("movieId", movieId)
                .getResultList();

        return titles.isEmpty() ? null : titles.get(0);
    }

    private String findCinemaNameById(Long cinemaId) {
        if (cinemaId == null) {
            return null;
        }

        String jpql = """
                SELECT c.name
                FROM CinemaJpaEntity c
                WHERE c.id = :cinemaId
                  AND c.deleted = false
                """;

        List<String> names = entityManager
                .createQuery(jpql, String.class)
                .setParameter("cinemaId", cinemaId)
                .getResultList();

        return names.isEmpty() ? null : names.get(0);
    }

    private boolean isInvalidFilter(
            Long cinemaId,
            String cinemaName,
            Long movieId,
            String movieTitle
    ) {
        boolean cinemaFilterInvalid = cinemaId != null && cinemaName == null;
        boolean movieFilterInvalid = movieId != null && movieTitle == null;

        return cinemaFilterInvalid || movieFilterInvalid;
    }
}
