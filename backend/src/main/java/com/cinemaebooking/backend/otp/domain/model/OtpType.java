package com.cinemaebooking.backend.otp.domain.model;

/**
 * OtpType - Enum định nghĩa các loại OTP trong hệ thống.
 *
 * <p>Mỗi loại OTP phục vụ một mục đích khác nhau:
 * <ul>
 *   <li>EMAIL_VERIFICATION: Xác minh email khi đăng ký tài khoản mới</li>
 *   <li>PASSWORD_RESET: Khôi phục mật khẩu khi user quên</li>
 * </ul>
 *
 */
public enum OtpType {
    EMAIL_VERIFICATION,
    PASSWORD_RESET
}
