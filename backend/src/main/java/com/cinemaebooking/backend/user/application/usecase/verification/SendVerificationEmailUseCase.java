package com.cinemaebooking.backend.user.application.usecase.verification;

import com.cinemaebooking.backend.otp.application.dto.RegisterWithOtpRequest;
import com.cinemaebooking.backend.otp.application.dto.SendOtpResponse;
import com.cinemaebooking.backend.otp.application.port.OtpService;
import com.cinemaebooking.backend.user.application.dto.AuthDTO.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * SendVerificationEmailUseCase - Gửi email chứa mã OTP để xác minh email.
 *
 * <p>Lớp này đóng vai trò adapter: nhận RegisterRequest (từ AuthController),
 * convert sang RegisterWithOtpRequest, rồi delegate xuống OtpService.
 *
 * <p>Việc tách riêng UseCase này giúp:
 * <ul>
 *   <li>Giữ backward compatibility với các chỗ khác đã dùng SendVerificationEmailUseCase</li>
 *   <li>Có thể thêm logic validation/transform trước khi gọi OtpService</li>
 *   <li>Controller chỉ biết UseCase, không biết OtpService trực tiếp</li>
 * </ul>
 *
 * <p>Luồng:
 * <pre>
 * AuthController.register()
 *   → SendVerificationEmailUseCase.execute(RegisterRequest)
 *       → OtpService.registerAndSendOtp(RegisterWithOtpRequest)
 *           → Tạo user INACTIVE
 *           → Tạo OTP
 *           → Gửi email
 *           → Trả SendOtpResponse
 * </pre>
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Service
@RequiredArgsConstructor
public class SendVerificationEmailUseCase {

    private final OtpService otpService;

    /**
     * Thực hiện đăng ký và gửi OTP.
     *
     * <p>Convert RegisterRequest (sử dụng LocalDate, UserGender)
     * sang RegisterWithOtpRequest (sử dụng String) rồi delegate sang OtpService.
     *
     * @param request thông tin đăng ký từ user
     * @return SendOtpResponse chứa userId và thời điểm hết hạn OTP
     */
    public SendOtpResponse execute(RegisterRequest request) {
        // Convert từ RegisterRequest (domain types) sang RegisterWithOtpRequest (String types)
        RegisterWithOtpRequest otpRequest = RegisterWithOtpRequest.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                // Convert LocalDate → String (yyyy-MM-dd) để tránh phụ thuộc type cụ thể
                .dateOfBirth(request.getDateOfBirth() != null ? request.getDateOfBirth().toString() : null)
                // Convert UserGender enum → String để tránh phụ thuộc type cụ thể
                .gender(request.getGender() != null ? request.getGender().name() : null)
                .build();

        return otpService.registerAndSendOtp(otpRequest);
    }

    /**
     * Gửi lại mã OTP cho user đã đăng ký nhưng chưa verify.
     *
     * @param userId ID của user cần gửi lại OTP
     * @return SendOtpResponse chứa OTP mới
     */
    public SendOtpResponse resend(Long userId) {
        return otpService.resendOtp(userId);
    }
}
