package com.cinemaebooking.backend.payment.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.application.usecase.ConfirmPaymentUseCase;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.booking_coupon.infrastructure.persistence.entity.BookingCouponJpaEntity;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.booking_combo.infrastructure.persistence.entity.BookingComboJpaEntity;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.common.exception.domain.PaymentExceptions;
import com.cinemaebooking.backend.notification.application.dto.BookingEmailData;
import com.cinemaebooking.backend.notification.application.port.NotificationService;
import com.cinemaebooking.backend.payment.application.port.PaymentRepository;
import com.cinemaebooking.backend.payment.domain.model.Payment;
import com.cinemaebooking.backend.ticket.infrastructure.persistence.entity.TicketJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * CompletePaymentUseCase - Xử lý webhook/callback từ cổng thanh toán online.
 */
@Service
@RequiredArgsConstructor
public class CompletePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingJpaRepository bookingJpaRepository;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final NotificationService notificationService;

    @Transactional
    public void execute(String paymentCode) {
        execute(paymentCode, "TXN-" + System.nanoTime(), "{ \"source\": \"gateway\" }");
    }

    @Transactional
    public void execute(String paymentCode, String transactionId, String providerResponse) {
        Payment payment = paymentRepository.findByPaymentCode(paymentCode);
        if (payment == null) {
            throw PaymentExceptions.notFound(paymentCode);
        }

        if (payment.checkExpired()) {
            paymentRepository.save(payment);
            throw PaymentExceptions.expired(paymentCode);
        }

        Long bookingId = payment.getBookingId();
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));

        payment.markSuccess(transactionId, providerResponse);
        paymentRepository.save(payment);

        confirmPaymentUseCase.execute(bookingId, payment);

        sendPaymentSuccessNotification(bookingId, payment.getId().getValue());
    }

    private void sendPaymentSuccessNotification(Long bookingId, Long paymentId) {
        bookingJpaRepository.findByIdWithDetails(bookingId)
                .ifPresent(entity -> {
                    var user = entity.getUser();
                    BookingEmailData bookingData = mapToBookingEmailData(entity);

                    notificationService.sendPaymentSuccessNotification(
                            user.getId(),
                            user.getEmail(),
                            user.getFullName(),
                            bookingData
                    );
                });
    }

    private BookingEmailData mapToBookingEmailData(BookingJpaEntity entity) {
        BookingEmailData.BookingEmailDataBuilder builder = BookingEmailData.builder()
                .bookingId(entity.getId())
                .bookingCode(entity.getBookingCode())
                .userId(entity.getUser().getId())
                .movieTitle(entity.getMovieTitle())
                .cinemaName(entity.getCinemaName())
                .roomName(entity.getRoomName())
                .showtimeStartTime(entity.getShowtimeStartTime())
                .finalAmount(entity.getFinalAmount());

        Set<TicketJpaEntity> tickets = entity.getTickets();
        if (tickets != null && !tickets.isEmpty()) {
            var seatDataList = tickets.stream()
                    .map(t -> BookingEmailData.SeatData.builder()
                            .seatName(t.getSeatName())
                            .seatType(t.getSeatType())
                            .price(t.getPrice())
                            .build())
                    .toList();
            builder.seats(seatDataList);
        }

        Set<BookingComboJpaEntity> combos = entity.getCombos();
        if (combos != null && !combos.isEmpty()) {
            var comboDataList = combos.stream()
                    .map(c -> BookingEmailData.ComboData.builder()
                            .comboName(c.getComboName())
                            .quantity(c.getQuantity())
                            .unitPrice(c.getUnitPrice())
                            .build())
                    .toList();
            builder.combos(comboDataList);
        }

        BookingCouponJpaEntity couponEntity = entity.getCoupon();
        if (couponEntity != null) {
            builder.coupon(BookingEmailData.CouponData.builder()
                    .code(couponEntity.getCouponCode())
                    .discountAmount(couponEntity.getDiscountAmount())
                    .build());
        }

        return builder.build();
    }
}
