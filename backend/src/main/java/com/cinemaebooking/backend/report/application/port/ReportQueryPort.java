package com.cinemaebooking.backend.report.application.port;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.cinemaebooking.backend.refund.infrastructure.persistence.entity.RefundJpaEntity;
import com.cinemaebooking.backend.showtime.infrastructure.persistence.entity.ShowtimeJpaEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportQueryPort {

    List<BookingJpaEntity> findBookingsByCreatedAt(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long cinemaId,
            Long movieId
    );

    List<BookingJpaEntity> findConfirmedBookingsByPaidAt(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long cinemaId,
            Long movieId
    );

    List<BookingJpaEntity> findConfirmedBookingsByShowtimeStartTime(
            Instant fromDate,
            Instant toDate,
            Long cinemaId,
            Long movieId
    );

    List<PaymentJpaEntity> findSuccessfulPaymentsByPaidAt(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long cinemaId,
            Long movieId
    );

    List<RefundJpaEntity> findRefundsByProcessedAt(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long cinemaId,
            Long movieId
    );

    List<RefundJpaEntity> findRefundsByRequestedAt(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Long cinemaId,
            Long movieId
    );

    List<ShowtimeJpaEntity> findShowtimesByStartTime(
            Instant fromDate,
            Instant toDate,
            Long cinemaId,
            Long movieId
    );
}
