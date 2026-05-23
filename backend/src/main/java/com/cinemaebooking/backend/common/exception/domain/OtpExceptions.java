package com.cinemaebooking.backend.common.exception.domain;

import com.cinemaebooking.backend.common.exception.BaseException;
import com.cinemaebooking.backend.common.exception.ErrorCategory;
import com.cinemaebooking.backend.common.exception.ErrorCode;
import com.cinemaebooking.backend.common.exception.ErrorDetail;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OtpExceptions - Domain-specific exceptions cho OTP operations.
 *
 * <p>Cung cấp các factory method tạo exception với semantic rõ ràng.
 * Tất cả exceptions đều map sang ErrorCode tương ứng trong ErrorCode enum.
 *
 * <p>Error codes dành riêng cho OTP: 2080–2085
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OtpExceptions {

    /**
     * Không tìm thấy OTP active nào cho user này.
     * Có thể do: OTP đã bị xóa, đã verify, hoặc đã hết hạn.
     */
    public static BaseException notFound(Long userId) {
        return new BaseException(ErrorCode.OTP_NOT_FOUND,
                "Không tìm thấy mã OTP cho người dùng: " + userId);
    }

    /**
     * Mã OTP đã hết thời gian hiệu lực (quá 5 phút).
     * User cần gọi resend-otp để nhận mã mới.
     */
    public static BaseException expired() {
        return new BaseException(ErrorCode.OTP_EXPIRED,
                "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
    }

    /**
     * Mã OTP không khớp với mã đã gửi.
     * Cảnh báo: số lần sai được track ở OtpJpaEntity.attempts.
     */
    public static BaseException incorrect() {
        return new BaseException(ErrorCode.OTP_INCORRECT,
                "Mã OTP không đúng. Vui lòng kiểm tra lại.");
    }

    /**
     * User đã nhập sai quá 5 lần.
     * Phải gọi resend-otp để tạo OTP mới (reset attempts).
     */
    public static BaseException maxAttemptsReached() {
        return new BaseException(ErrorCode.OTP_MAX_ATTEMPTS_REACHED,
                "Đã nhập sai quá 5 lần. Vui lòng yêu cầu mã mới.");
    }

    /**
     * User đã verify email trước đó rồi (status = ACTIVE).
     * Không cần verify nữa.
     */
    public static BaseException userAlreadyVerified(Long userId) {
        return new BaseException(ErrorCode.OTP_USER_ALREADY_VERIFIED,
                "Tài khoản đã được xác minh trước đó. Không cần xác minh lại.");
    }

    /**
     * User gọi resend-otp quá sớm (chưa đủ 60 giây kể từ OTP cuối).
     * Anti-spam mechanism: ngăn user spam request gửi email liên tục.
     */
    public static BaseException resendTooSoon() {
        return new BaseException(ErrorCode.OTP_RESEND_TOO_SOON,
                List.of(new ErrorDetail("otp", ErrorCategory.TIME_CONSTRAINT,
                        "Vui lòng đợi 60 giây trước khi gửi lại mã OTP")));
    }

    /**
     * Email đã được sử dụng bởi tài khoản khác (kể cả đã xóa mềm).
     * Đây là lỗi conflict, map sang USER_EMAIL_ALREADY_EXISTS (2008).
     */
    public static BaseException emailAlreadyExists(String email) {
        return new BaseException(ErrorCode.USER_EMAIL_ALREADY_EXISTS,
                "Email '" + email + "' đã được sử dụng bởi tài khoản khác.");
    }
}
