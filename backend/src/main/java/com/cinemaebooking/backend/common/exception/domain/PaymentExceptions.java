package com.cinemaebooking.backend.common.exception.domain;

import com.cinemaebooking.backend.common.exception.BaseException;
import com.cinemaebooking.backend.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PaymentExceptions - Factory methods for payment domain exceptions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentExceptions {

    // ================== NOT FOUND ==================
    public static BaseException notFound(String paymentCode) {
        return new BaseException(
                ErrorCode.PAYMENT_NOT_FOUND,
                "Không tìm thấy thanh toán: " + paymentCode
        );
    }

    // ================== DUPLICATE ==================
    public static BaseException duplicateTransactionId(String transactionId) {
        return new BaseException(
                ErrorCode.PAYMENT_ALREADY_EXISTS,
                "Thanh toán đã tồn tại cho transactionId: " + transactionId
        );
    }

    // ================== EXPIRED ==================
    public static BaseException expired(String paymentCode) {
        return new BaseException(
                ErrorCode.PAYMENT_EXPIRED,
                "Phiên thanh toán đã hết hạn: " + paymentCode
        );
    }

    // ================== INVALID STATUS ==================
    public static BaseException invalidStatus(String paymentCode, String status) {
        return new BaseException(
                ErrorCode.PAYMENT_INVALID_STATUS,
                "Trạng thái thanh toán không hợp lệ: " + status + " cho payment " + paymentCode
        );
    }

    // ================== METHOD NOT SUPPORTED ==================
    public static BaseException methodNotSupported(String method) {
        return new BaseException(
                ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED,
                "Phương thức thanh toán không được hỗ trợ: " + method
        );
    }

    // ================== PAYMENT FAILED (4503) ==================
    public static BaseException failed(String paymentCode, String reason) {
        return new BaseException(
                ErrorCode.PAYMENT_FAILED,
                "Thanh toán thất bại: " + reason + " (payment: " + paymentCode + ")"
        );
    }

    // ================== INVALID AMOUNT (4504) ==================
    public static BaseException invalidAmount(Long expected, Long actual) {
        return new BaseException(
                ErrorCode.PAYMENT_INVALID_AMOUNT,
                "Số tiền thanh toán không hợp lệ. Expected: " + expected + ", actual: " + actual
        );
    }

    // ================== DUPLICATE REQUEST (4508) ==================
    public static BaseException duplicateRequest(String paymentCode) {
        return new BaseException(
                ErrorCode.PAYMENT_DUPLICATE_REQUEST,
                "Yêu cầu thanh toán trùng lặp cho payment: " + paymentCode
        );
    }

    // ================== GATEWAY TIMEOUT (4509) ==================
    public static BaseException gatewayTimeout(String paymentCode) {
        return new BaseException(
                ErrorCode.PAYMENT_GATEWAY_TIMEOUT,
                "Cổng thanh toán không phản hồi cho payment: " + paymentCode
        );
    }
}