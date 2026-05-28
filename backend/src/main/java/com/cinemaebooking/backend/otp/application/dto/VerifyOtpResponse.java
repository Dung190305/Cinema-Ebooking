package com.cinemaebooking.backend.otp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * VerifyOtpResponse - Response trả về sau khi xác minh OTP.
 *
 * <p>Frontend dựa vào:
 * <ul>
 *   <li>success = true → chuyển sang trang đăng nhập</li>
 *   <li>success = false → hiển thị error message</li>
 * </ul>
 *
 * @author ducthinhn
 * @since 2026
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpResponse {

    /**
     * Kết quả xác minh.
     * true = thành công, user đã được kích hoạt
     * false = thất bại (code sai, hết hạn, v.v.)
     */
    private boolean success;

    /**
     * Thông báo mô tả kết quả.
     * success = true: "Xác minh thành công! Tài khoản của bạn đã được kích hoạt"
     * success = false: message được override bởi exception handler
     */
    private String message;

    /** ID của user đã được xác minh (hoặc đang thử verify). */
    private Long userId;

    /**
     * Cờ cho biết tài khoản đã được kích hoạt hay chưa.
     * = true khi success = true (duplicated for convenience)
     */
    private boolean isActivated;
}
