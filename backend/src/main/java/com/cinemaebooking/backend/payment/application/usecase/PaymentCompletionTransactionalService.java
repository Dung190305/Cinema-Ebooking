package com.cinemaebooking.backend.payment.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.application.usecase.ConfirmPaymentUseCase;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.common.exception.domain.PaymentExceptions;
import com.cinemaebooking.backend.notification.application.dto.PaymentSuccessOutboxPayload;
import com.cinemaebooking.backend.notification.application.usecase.OutboxService;
import com.cinemaebooking.backend.payment.application.dto.CompletePaymentResponse;
import com.cinemaebooking.backend.payment.application.port.PaymentRepository;
import com.cinemaebooking.backend.payment.domain.enums.PaymentStatus;
import com.cinemaebooking.backend.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * PaymentCompletionTransactionalService — Finalize payment trong một transaction.
 *
 * <p>Thứ tự bên trong transaction:
 * <ol>
 *   <li>Tìm Payment + Booking, validate không expired</li>
 *   <li>payment.markSuccess() → lưu payment</li>
 *   <li>confirmPaymentUseCase → booking CONFIRMED + tạo Ticket</li>
 *   <li>outboxService → lưu outbox event (PAYMENT_SUCCESS)</li>
 * </ol>
 *
 * <p>Outbox event được ghi vào cùng transaction với payment/booking.
 * Email thực tế được gửi bởi OutboxJob (scheduled) đọc từ bảng outbox_events.
 * Nếu OutboxJob gửi fail → retry tối đa 3 lần.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCompletionTransactionalService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingJpaRepository bookingJpaRepository;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final OutboxService outboxService;

    @Transactional
    public CompletePaymentResponse complete(String paymentCode) {
        String transactionId = "TXN-" + System.nanoTime();
        String providerResponse = "{ \"source\": \"gateway\" }";

        Payment payment = paymentRepository.findByPaymentCode(paymentCode);
        if (payment == null) {
            throw PaymentExceptions.notFound(paymentCode);
        }

        if (payment.checkExpired()) {
            paymentRepository.save(payment);
            throw PaymentExceptions.expired(paymentCode);
        }

        Long bookingId = payment.getBookingId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));

        payment.markSuccess(transactionId, providerResponse);
        paymentRepository.save(payment);

        confirmPaymentUseCase.execute(bookingId, payment);

        log.info("Payment complete, saving outbox event for bookingId={}", bookingId);
        saveOutboxEvent(bookingId);

        return new CompletePaymentResponse(
                PaymentStatus.SUCCESS,
                booking.getId().getValue(),
                booking.getShowtimeId(),
                "Thanh toán thành công",
                payment.getTransactionId()
        );
    }

    private void saveOutboxEvent(Long bookingId) {
        BookingJpaEntity entity = bookingJpaRepository.findByIdWithDetails(bookingId).orElse(null);
        if (entity == null) {
            log.warn("Booking not found for outbox event: bookingId={}", bookingId);
            return;
        }

        var user = entity.getUser();

        PaymentSuccessOutboxPayload payload = PaymentSuccessOutboxPayload.builder()
                .bookingId(entity.getId())
                .bookingCode(entity.getBookingCode())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userName(user.getFullName())
                .movieTitle(entity.getMovieTitle())
                .cinemaName(entity.getCinemaName())
                .roomName(entity.getRoomName())
                .showtimeStartTime(entity.getShowtimeStartTime())
                .finalAmount(entity.getFinalAmount())
                .seats(mapSeats(entity.getTickets()))
                .combos(mapCombos(entity.getCombos()))
                .coupon(mapCoupon(entity.getCoupon()))
                .build();

        outboxService.savePaymentSuccessEvent(payload);
    }

    private java.util.List<PaymentSuccessOutboxPayload.SeatData> mapSeats(Set<com.cinemaebooking.backend.ticket.infrastructure.persistence.entity.TicketJpaEntity> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return null;
        }
        return tickets.stream()
                .map(t -> PaymentSuccessOutboxPayload.SeatData.builder()
                        .seatName(t.getSeatName())
                        .seatType(t.getSeatType())
                        .price(t.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    private java.util.List<PaymentSuccessOutboxPayload.ComboData> mapCombos(Set<com.cinemaebooking.backend.booking_combo.infrastructure.persistence.entity.BookingComboJpaEntity> combos) {
        if (combos == null || combos.isEmpty()) {
            return null;
        }
        return combos.stream()
                .map(c -> PaymentSuccessOutboxPayload.ComboData.builder()
                        .comboName(c.getComboName())
                        .quantity(c.getQuantity())
                        .unitPrice(c.getUnitPrice())
                        .build())
                .collect(Collectors.toList());
    }

    private PaymentSuccessOutboxPayload.CouponData mapCoupon(com.cinemaebooking.backend.booking_coupon.infrastructure.persistence.entity.BookingCouponJpaEntity coupon) {
        if (coupon == null) {
            return null;
        }
        return PaymentSuccessOutboxPayload.CouponData.builder()
                .code(coupon.getCouponCode())
                .discountAmount(coupon.getDiscountAmount())
                .build();
    }
}
