package com.cinemaebooking.backend.user.application.usecase.verification;

import com.cinemaebooking.backend.otp.application.dto.VerifyOtpRequest;
import com.cinemaebooking.backend.otp.application.dto.VerifyOtpResponse;
import com.cinemaebooking.backend.otp.application.port.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * VerifyEmailUseCase - Xác minh mã OTP để kích hoạt tài khoản.
 *
 * <p>Lớp này đóng vai trò adapter: nhận code và userId từ Controller,
 * wrap thành VerifyOtpRequest rồi delegate xuống OtpService.
 *
 * <p>Tương tự SendVerificationEmailUseCase, việc tách riêng giúp:
 * <ul>
 *   <li>Giữ backward compatibility với các chỗ đã dùng VerifyEmailUseCase</li>
 *   <li>Có thể thêm logic validation/transform trước khi gọi OtpService</li>
 *   <li>Tách biệt presentation layer và application logic</li>
 * </ul>
 *
 * <p>Luồng:
 * <pre>
 * AuthController.verifyOtp()
 *   → VerifyEmailUseCase.execute(code, userId)
 *       → OtpService.verifyOtp(code, userId)
 *           → Kiểm tra user, OTP
 *           → user.activate()
 *           → Trả VerifyOtpResponse
 * </pre>
 *
 * @author ducthinhn
 * @since 2026
 */
@Service
@RequiredArgsConstructor
public class VerifyEmailUseCase {

    private final OtpService otpService;

    /**
     * Xác minh mã OTP và kích hoạt tài khoản user.
     *
     * @param code   mã OTP 6 chữ số do user nhập vào
     * @param userId ID của user đang thực hiện verify
     * @return VerifyOtpResponse chứa kết quả xác minh
     */
    public VerifyOtpResponse execute(String code, Long userId) {
        // Wrap thành VerifyOtpRequest để OtpService nhận
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .code(code)
                .userId(userId)
                .build();
        return otpService.verifyOtp(code, userId);
    }
}
