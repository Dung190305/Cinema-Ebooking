package com.cinemaebooking.backend.payment.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.payment.application.dto.CreatePaymentRequest;
import com.cinemaebooking.backend.payment.application.dto.CreatePaymentResponse;
import com.cinemaebooking.backend.payment.application.port.PaymentRepository;
import com.cinemaebooking.backend.payment.domain.enums.PaymentStatus;
import com.cinemaebooking.backend.payment.domain.model.Payment;
import com.cinemaebooking.backend.seat_lock.application.port.SeatLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreatePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final SeatLockService seatLockService;

    private static final int PAYMENT_DURATION_MINUTES = 10;
    private static final String GATEWAY_BASE_URL = "http://localhost:3000";

    @Transactional
    public CreatePaymentResponse execute(CreatePaymentRequest request) {

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> BookingExceptions.notFound(
                        BookingId.of(request.getBookingId())
                ));

        LocalDateTime paymentExpiredAt = LocalDateTime.now().plusMinutes(PAYMENT_DURATION_MINUTES);
        String paymentCode = "PAY-" + System.currentTimeMillis();

        Payment payment = Payment.builder()
                .paymentCode(paymentCode)
                .bookingId(booking.getId().getValue())
                .amount(booking.getFinalAmount())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .expiredAt(paymentExpiredAt)
                .build();

        paymentRepository.save(payment);

        // Extend seat locks cho đến khi payment hết hạn — đảm bảo ghế không bị
        // expired lock giữa chừng trong khi user đang ở trang thanh toán
        seatLockService.extendLocksForPayment(
                booking.getId().getValue(),
                booking.getUserId(),
                booking.getShowtimeId(),
                paymentExpiredAt
        );

        String paymentUrl = String.format(
                "%s/payment/%s?paymentCode=%s&amount=%s&bookingId=%d&showtimeId=%d&callbackUrl=%s",
                GATEWAY_BASE_URL,
                request.getMethod().name().toLowerCase(),
                paymentCode,
                booking.getFinalAmount().toString(),
                booking.getId().getValue(),
                booking.getShowtimeId(),
                request.getCallbackUrl()
        );

        return CreatePaymentResponse.builder()
                .paymentCode(paymentCode)
                .paymentUrl(paymentUrl)
                .expiredAt(paymentExpiredAt)
                .build();
    }
}