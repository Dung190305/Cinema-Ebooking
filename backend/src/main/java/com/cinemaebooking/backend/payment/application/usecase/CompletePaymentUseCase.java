package com.cinemaebooking.backend.payment.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.application.usecase.ConfirmPaymentUseCase;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.booking_coupon.infrastructure.persistence.entity.BookingCouponJpaEntity;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.booking_combo.infrastructure.persistence.entity.BookingComboJpaEntity;
import com.cinemaebooking.backend.common.exception.BaseException;
import com.cinemaebooking.backend.common.exception.ErrorCode;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.common.exception.domain.PaymentExceptions;
import com.cinemaebooking.backend.notification.application.dto.BookingEmailData;
import com.cinemaebooking.backend.notification.application.port.NotificationService;
import com.cinemaebooking.backend.payment.application.dto.CompletePaymentResponse;
import com.cinemaebooking.backend.payment.application.port.PaymentRepository;
import com.cinemaebooking.backend.payment.domain.enums.PaymentStatus;
import com.cinemaebooking.backend.payment.domain.model.Payment;
import com.cinemaebooking.backend.ticket.infrastructure.persistence.entity.TicketJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * CompletePaymentUseCase - Xử lý webhook/callback từ cổng thanh toán online.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompletePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingJpaRepository bookingJpaRepository;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final NotificationService notificationService;
    private final PaymentCompletionTransactionalService transactionalService;

    /**
     * Xử lý payment callback và trả về kết quả dưới dạng DTO.
     * Phương thức này luôn trả về CompletePaymentResponse (không throw exception ra controller).
     *
     * @param paymentCode Mã thanh toán từ gateway
     * @return CompletePaymentResponse chứa trạng thái và thông tin liên quan
     */
    public CompletePaymentResponse executeAndGetResponse(String paymentCode) {
        try {
            return transactionalService.complete(paymentCode, "TXN-" + System.nanoTime(), "{ \"source\": \"gateway\" }");
        } catch (BaseException e) {
            // Xử lý các exception business từ PaymentExceptions
            if (e.getErrorCode() == ErrorCode.PAYMENT_EXPIRED) {
                log.warn("Payment expired: {}", paymentCode, e);
                return new CompletePaymentResponse(
                        PaymentStatus.EXPIRED,
                        null, null,
                        "Mã thanh toán đã hết hạn",
                        null
                );
            } else if (e.getErrorCode() == ErrorCode.PAYMENT_NOT_FOUND) {
                log.warn("Payment not found: {}", paymentCode, e);
                return new CompletePaymentResponse(
                        PaymentStatus.FAILED,
                        null, null,
                        "Không tìm thấy giao dịch",
                        null
                );
            } else {
                // Các lỗi business khác (ví dụ: PAYMENT_INVALID_STATUS, BOOKING_NOT_FOUND...)
                log.error("Business error during payment completion: {}", paymentCode, e);
                return new CompletePaymentResponse(
                        PaymentStatus.FAILED,
                        null, null,
                        e.getMessage() != null ? e.getMessage() : "Lỗi xử lý thanh toán",
                        null
                );
            }
        } catch (Exception e) {
            // Lỗi không mong đợi (runtime, technical)
            log.error("Unexpected error during payment completion: {}", paymentCode, e);
            return new CompletePaymentResponse(
                    PaymentStatus.FAILED,
                    null, null,
                    "Lỗi xử lý thanh toán: " + e.getMessage(),
                    null
            );
        }
    }

    /**
     * Backward compatible method for existing calls (e.g., webhook) that don't need response.
     */
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