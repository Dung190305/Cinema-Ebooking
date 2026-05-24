package com.cinemaebooking.backend.user.application.usecase.auth;

import com.cinemaebooking.backend.otp.application.dto.VerifyOtpResponse;
import com.cinemaebooking.backend.otp.application.port.OtpService;
import com.cinemaebooking.backend.user.application.dto.AuthDTO.ResetPasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCase {

    private final OtpService otpService;

    @Transactional
    public VerifyOtpResponse execute(ResetPasswordRequest request) {

        // Lấy dữ liệu từ Request DTO để truyền vào OtpService
        return otpService.verifyForgotPasswordOtpAndReset(
                request.getEmail(),
                request.getOtp(),
                request.getNewPassword()
        );
    }
}