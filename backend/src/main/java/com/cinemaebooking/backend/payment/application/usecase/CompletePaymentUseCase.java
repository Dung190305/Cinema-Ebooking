package com.cinemaebooking.backend.payment.application.usecase;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.application.usecase.ConfirmPaymentUseCase;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.common.exception.domain.BookingExceptions;
import com.cinemaebooking.backend.common.exception.domain.PaymentExceptions;
import com.cinemaebooking.backend.payment.application.port.PaymentRepository;
import com.cinemaebooking.backend.payment.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CompletePaymentUseCase - Xử lý webhook/callback từ cổng thanh toán online.
 *
 * <p>Trách nhiệm DUY NHẤT của UseCase này:
 * <ul>
 *   <li>Nhận paymentCode từ gateway</li>
 *   <li>Fetch & update Payment domain (markSuccess)</li>
 *   <li>Delegate sang ConfirmPaymentUseCase để hoàn tất booking lifecycle</li>
 * </ul>
 *
 * <p>KHÔNG làm: booking logic, seat logic, loyalty logic.
 * Những thứ đó thuộc về ConfirmPaymentUseCase — single source of truth.
 *
 * <p>Lý do tách biệt:
 * <ul>
 *   <li>Thanh toán online (webhook) và thanh toán tại quầy đều gọi cùng ConfirmPaymentUseCase</li>
 *   <li>Thay đổi business logic chỉ cần sửa MỘT chỗ</li>
 *   <li>ConfirmPaymentUseCase có thể dùng lại ở bất kỳ đâu mà không phụ thuộc payment gateway</li>
 * </ul>
 *
 */
@Service
@RequiredArgsConstructor
public class CompletePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    /**
     * Overload cho backward compatibility với controller hiện tại.
     * Controller chỉ truyền paymentCode, transactionId và providerResponse dùng mock.
     */
    @Transactional
    public void execute(String paymentCode) {
        execute(paymentCode, "TXN-" + System.nanoTime(), "{ \"source\": \"gateway\" }");
    }

    /**
     * Xử lý payment callback từ cổng thanh toán online.
     * Chỉ xử lý payment state — không làm bất kỳ booking logic nào.
     *
     * @param paymentCode      payment code từ gateway
     * @param transactionId    transaction ID từ payment provider (có thể null nếu chưa có)
     * @param providerResponse raw response từ provider (JSON string)
     */
    @Transactional
    public void execute(String paymentCode, String transactionId, String providerResponse) {
        // 1. Fetch Payment domain object
        Payment payment = paymentRepository.findByPaymentCode(paymentCode);
        if (payment == null) {
            throw PaymentExceptions.notFound(paymentCode);
        }

        // 2. Kiểm tra payment chưa expired
        if (payment.checkExpired()) {
            paymentRepository.save(payment);
            throw PaymentExceptions.expired(paymentCode);
        }

        // 3. Validate booking tồn tại (pre-check trước khi confirm)
        Long bookingId = payment.getBookingId();
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> BookingExceptions.notFound(BookingId.of(bookingId)));

        // 4. Payment domain: PENDING → SUCCESS
        payment.markSuccess(transactionId, providerResponse);
        paymentRepository.save(payment);

        // 5. Delegate to ConfirmPaymentUseCase — truyền payment để copy paidAt và release locks
        confirmPaymentUseCase.execute(bookingId, payment);
    }
}
