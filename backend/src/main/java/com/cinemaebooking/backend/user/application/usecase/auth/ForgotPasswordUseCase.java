package com.cinemaebooking.backend.user.application.usecase.auth;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.otp.application.dto.SendOtpResponse;
import com.cinemaebooking.backend.otp.application.port.OtpService;
import com.cinemaebooking.backend.user.application.port.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordUseCase {

    private final OtpService otpService;
    private final EmailService emailService;

    public SendOtpResponse execute(String email) {
        if (email == null || email.isBlank()) {
            throw CommonExceptions.invalidInput("Email không được để trống");
        }

        // 1. Tạo OTP khôi phục mật khẩu trong Database transaction
        // (Trong OtpService sẽ check email tồn tại và đã ACTIVE chưa)
        SendOtpResponse response = otpService.forgotPasswordAndSendOtp(email);

        // 2. Gửi Email ngoài Transaction để tránh treo/rollback DB nếu mail lỗi
        try {
            sendOtpEmail(response.getEmail(), response.getGeneratedOtpCode());
        } catch (Exception e) {
            log.error("Không thể gửi email OTP khôi phục mật khẩu đến {}: {}", response.getEmail(), e.getMessage());
        }

        return response;
    }

    private void sendOtpEmail(String to, String otpCode) {
        String subject = "Mã xác minh khôi phục mật khẩu - Cinema E-Booking";
        String body = String.format("""
                Xin chào,

                Chúng tôi nhận được yêu cầu khôi phục mật khẩu từ tài khoản của bạn.

                Mã OTP xác minh của bạn là: %s

                Mã này có hiệu lực trong 5 phút.
                Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.

                Trân trọng,
                Đội ngũ Cinema E-Booking
                """, otpCode);
        emailService.send(to, subject, body);
    }
}