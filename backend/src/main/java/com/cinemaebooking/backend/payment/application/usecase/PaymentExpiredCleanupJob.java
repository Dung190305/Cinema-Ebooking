package com.cinemaebooking.backend.payment.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.booking_coupon.application.usecase.ReleaseBookingCouponUseCase;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.payment.application.port.PaymentRepository;
import com.cinemaebooking.backend.payment.domain.enums.PaymentStatus;
import com.cinemaebooking.backend.payment.domain.model.Payment;
import com.cinemaebooking.backend.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.cinemaebooking.backend.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import com.cinemaebooking.backend.ticket.application.usecase.ReleaseSeatsUseCase;
import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ScheduledJob tự động hủy payment hết hạn và giải phóng ghế về AVAILABLE.
 *
 * <p>Chạy mỗi 1 phút, tìm tất cả payment có status = PENDING và expiredAt <= now,
 * đánh dấu EXPIRED, đồng thời hủy booking liên quan và trả ghế.
 *
 * <p>Lý do hủy cả booking khi payment hết hạn:
 * <ul>
 *   <li>Payment hết hạn = user không thanh toán trong 15 phút → booking không còn hiệu lực</li>
 *   <li>Ghế phải được trả về AVAILABLE để user khác có thể đặt</li>
 * </ul>
 *
 * @author ducthinhn
 * @since 2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpiredCleanupJob {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ReleaseSeatsUseCase releaseSeatsUseCase;
    private final ReleaseBookingCouponUseCase releaseCouponUseCase;

    /**
     * Chạy mỗi 1 phút để cleanup payment hết hạn.
     * Dùng fixedDelay để đảm bảo job không chồng chéo nhau.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cleanupExpiredPayments() {
        LocalDateTime now = LocalDateTime.now();

        List<PaymentJpaEntity> expiredPayments = paymentJpaRepository
                .findAllByStatusAndDeletedFalse(PaymentStatus.PENDING)
                .stream()
                .filter(p -> p.getExpiredAt() != null && !p.getExpiredAt().isAfter(now))
                .toList();

        if (expiredPayments.isEmpty()) {
            return;
        }

        log.info("[PaymentExpiredCleanupJob] Found {} expired payments to cleanup", expiredPayments.size());

        for (PaymentJpaEntity entity : expiredPayments) {
            try {
                processExpiredPayment(entity, now);
            } catch (Exception e) {
                log.error("[PaymentExpiredCleanupJob] Failed to process expired payment: {}",
                        entity.getPaymentCode(), e);
            }
        }
    }

    private void processExpiredPayment(PaymentJpaEntity entity, LocalDateTime now) {
        Long bookingId = entity.getBooking().getId();

        // 1. Đánh dấu payment = EXPIRED
        Payment payment = paymentRepository.findByPaymentCode(entity.getPaymentCode());
        payment.checkExpired();
        paymentRepository.update(payment);

        log.info("[PaymentExpiredCleanupJob] Payment {} expired, canceling booking {}",
                entity.getPaymentCode(), bookingId);

        // 2. Hủy booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElse(null);

        if (booking == null) {
            log.warn("[PaymentExpiredCleanupJob] Booking {} not found for expired payment {}",
                    bookingId, entity.getPaymentCode());
            return;
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            log.info("[PaymentExpiredCleanupJob] Booking {} is not PENDING (status={}), skip canceling",
                    bookingId, booking.getStatus());
            return;
        }

        // 3. Booking domain: PENDING → CANCELLED
        booking.cancel();

        // 4. Tickets: CANCELLED
        booking.getTickets().forEach(Ticket::cancel);

        // 5. Giải phóng ghế về AVAILABLE
        releaseSeatsUseCase.execute(booking.getShowtimeId(), booking.getTickets());

        // 6. Giải phóng coupon nếu có
        releaseCouponUseCase.execute(booking.getId().getValue());

        // 7. Persist
        bookingRepository.save(booking);

        log.info("[PaymentExpiredCleanupJob] Successfully canceled expired booking {} (payment: {})",
                bookingId, entity.getPaymentCode());
    }
}
