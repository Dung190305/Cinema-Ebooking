package com.cinemaebooking.backend.payment.application.usecase;

import com.cinemaebooking.backend.common.exception.BaseException;
import com.cinemaebooking.backend.common.exception.ErrorCode;
import com.cinemaebooking.backend.payment.application.dto.CompletePaymentResponse;
import com.cinemaebooking.backend.payment.domain.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * CompletePaymentUseCase - Xử lý webhook/callback từ cổng thanh toán online.
 *
 * <p>Entry point cho controller: nhận paymentCode từ gateway,
 * ủy thác toàn bộ logic cho PaymentCompletionTransactionalService.
 * Luôn trả về CompletePaymentResponse — không throw exception ra ngoài.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompletePaymentUseCase {

    private final PaymentCompletionTransactionalService transactionalService;

    public CompletePaymentResponse executeAndGetResponse(String paymentCode) {
        try {
            return transactionalService.complete(paymentCode);
        } catch (BaseException e) {
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
                log.error("Business error during payment completion: {}", paymentCode, e);
                return new CompletePaymentResponse(
                        PaymentStatus.FAILED,
                        null, null,
                        e.getMessage() != null ? e.getMessage() : "Lỗi xử lý thanh toán",
                        null
                );
            }
        } catch (Exception e) {
            log.error("Unexpected error during payment completion: {}", paymentCode, e);
            return new CompletePaymentResponse(
                    PaymentStatus.FAILED,
                    null, null,
                    "Lỗi xử lý thanh toán: " + e.getMessage(),
                    null
            );
        }
    }
}
