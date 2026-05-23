package com.cinemaebooking.backend.otp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SendOtpResponse - Response trả về sau khi đăng ký hoặc gửi lại OTP.
 *
 * <p>Frontend cần lưu trữ userId và expiresAt để:
 * <ul>
 *   <li>Dùng userId cho verify-otp và resend-otp</li>
 *   <li>Hiển thị countdown timer đến khi OTP hết hạn</li>
 *   <li>Biết thời điểm hết hạn để tự động thông báo user</li>
 * </ul>
 *
 * @author ducthinhn
 * @since 2026
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendOtpResponse {

    /**
     * Thông báo mô tả kết quả (hiển thị cho user).
     * Ví dụ: "Mã OTP đã được gửi đến email của bạn"
     */
    private String message;

    /**
     * ID của user vừa được tạo hoặc đang chờ verify.
     * Frontend cần lưu lại để gọi verify-otp và resend-otp.
     */
    private Long userId;

    /**
     * Email mà OTP đã được gửi đến.
     * Frontend có thể hiển thị lại để user biết đã gửi đúng email.
     */
    private String email;

    /**
     * Thời điểm OTP hết hiệu lực.
     * Frontend dùng để:
     * - Hiển thị countdown timer
     * - Disable nút verify khi hết hạn
     * - Show message yêu cầu resend khi hết hạn
     */
    private LocalDateTime expiresAt;
}
