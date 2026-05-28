package com.cinemaebooking.backend.user.application.usecase.verification;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.otp.application.dto.RegisterWithOtpRequest;
import com.cinemaebooking.backend.otp.application.dto.SendOtpResponse;
import com.cinemaebooking.backend.otp.application.port.OtpService;
import com.cinemaebooking.backend.user.application.dto.AuthDTO.RegisterRequest;
import com.cinemaebooking.backend.user.application.port.EmailService;
import com.cinemaebooking.backend.user.application.validator.Auth.RegisterValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendVerificationEmailUseCase {

    private final OtpService otpService;
    private final RegisterValidator registerValidator;
    private final EmailService emailService;

    public SendOtpResponse execute(RegisterRequest request) {
        if (request == null) {
            throw CommonExceptions.invalidInput("Register request must not be null");
        }

        // Thực hiện validation sớm tại tầng Application (Fail-fast)
        registerValidator.validate(request);

        RegisterWithOtpRequest otpRequest = RegisterWithOtpRequest.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth() != null ? request.getDateOfBirth().toString() : null)
                .gender(request.getGender() != null ? request.getGender().name() : null)
                .build();

        // 1. Lưu thông tin xuống Database (Chạy & Commit biệt lập trong Transaction)
        SendOtpResponse response = otpService.registerAndSendOtp(otpRequest);

        // 2. Gửi Email ngoài Transaction (Nếu mail lỗi, DB vẫn toàn vẹn, user có thể "Resend")
        try {
            sendOtpEmail(response.getEmail(), response.getGeneratedOtpCode());
        } catch (Exception e) {
            log.error("Không thể gửi email OTP đến {}: {}", response.getEmail(), e.getMessage());
            // Tuỳ chọn: Có thể throw custom exception hoặc cứ trả về response bình thường
            // để client hiển thị nút "Gửi lại mã" mà không làm mất thông tin tài khoản đã tạo.
        }

        return response;
    }

    public SendOtpResponse resend(Long userId) {
        // 1. Tạo OTP mới trong Database transaction
        SendOtpResponse response = otpService.resendOtp(userId);

        // 2. Gửi mail ngoài Transaction
        try {
            sendOtpEmail(response.getEmail(), response.getGeneratedOtpCode());
        } catch (Exception e) {
            log.error("Không thể gửi lại email OTP cho userId {}: {}", userId, e.getMessage());
        }

        return response;
    }

    private void sendOtpEmail(String to, String otpCode) {
        String subject = "Mã xác minh đăng ký tài khoản - Cinema E-Booking";
        String body = String.format("""
                Xin chào,

                Cảm ơn bạn đã đăng ký tài khoản tại Cinema E-Booking.

                Mã xác minh của bạn là: %s

                Mã này có hiệu lực trong 5 phút.
                Vui lòng không chia sẻ mã này với bất kỳ ai.

                Trân trọng,
                Đội ngũ Cinema E-Booking
                """, otpCode);
        emailService.send(to, subject, body);
    }
}