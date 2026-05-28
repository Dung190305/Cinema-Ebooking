package com.cinemaebooking.backend.payment.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.application.usecase.ConfirmPaymentUseCase;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.common.exception.domain.PaymentExceptions;
import com.cinemaebooking.backend.payment.application.dto.CompletePaymentResponse;
import com.cinemaebooking.backend.payment.application.port.PaymentRepository;
import com.cinemaebooking.backend.payment.domain.enums.PaymentStatus;
import com.cinemaebooking.backend.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCompletionTransactionalService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    @Transactional
    public CompletePaymentResponse complete(String paymentCode, String transactionId, String providerResponse) {
        // 1. Fetch Payment
        Payment payment = paymentRepository.findByPaymentCode(paymentCode);
        if (payment == null) {
            throw PaymentExceptions.notFound(paymentCode);
        }

        // 2. Check expiration
        if (payment.checkExpired()) {
            paymentRepository.save(payment);
            throw PaymentExceptions.expired(paymentCode);
        }

        // 3. Validate booking existence
        Long bookingId = payment.getBookingId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));

        // 4. Mark payment as SUCCESS
        payment.markSuccess(transactionId, providerResponse);
        paymentRepository.save(payment);

        // 5. Confirm booking (release locks, update status, etc.)
        confirmPaymentUseCase.execute(bookingId, payment);

        // 6. Build success response
        return new CompletePaymentResponse(
                PaymentStatus.SUCCESS,
                booking.getId().getValue(),
                booking.getShowtimeId(),
                "Thanh toán thành công",
                payment.getTransactionId()
        );
    }
}
