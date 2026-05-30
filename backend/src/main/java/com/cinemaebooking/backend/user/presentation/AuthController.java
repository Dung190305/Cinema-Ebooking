package com.cinemaebooking.backend.user.presentation;

import com.cinemaebooking.backend.otp.application.dto.SendOtpResponse;
import com.cinemaebooking.backend.otp.application.dto.VerifyForgotPasswordOtpRequest;
import com.cinemaebooking.backend.otp.application.dto.VerifyOtpRequest;
import com.cinemaebooking.backend.otp.application.dto.VerifyOtpResponse;
import com.cinemaebooking.backend.otp.application.port.OtpService;
import com.cinemaebooking.backend.user.application.dto.AuthDTO.LoginRequest;
import com.cinemaebooking.backend.user.application.dto.AuthDTO.RefreshTokenRequest;
import com.cinemaebooking.backend.user.application.dto.AuthDTO.RegisterRequest;
import com.cinemaebooking.backend.user.application.dto.AuthDTO.ResetPasswordRequest;
import com.cinemaebooking.backend.user.application.dto.Response.LoginResponse;
import com.cinemaebooking.backend.user.application.usecase.auth.*;
import com.cinemaebooking.backend.user.application.usecase.verification.SendVerificationEmailUseCase;
import com.cinemaebooking.backend.user.application.usecase.verification.VerifyEmailUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final OtpService otpService;

    // ================== REGISTER WITH OTP ==================

    /**
     * Bước 1: Đăng ký → tạo tài khoản INACTIVE + gửi OTP qua email
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public SendOtpResponse register(@Valid @RequestBody RegisterRequest request) {
        return sendVerificationEmailUseCase.execute(request);
    }

    /**
     * Bước 2: Xác minh OTP → ACTIVE tài khoản
     */
    @PostMapping("/verify-otp")
    public VerifyOtpResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return verifyEmailUseCase.execute(request.getCode(), request.getUserId());
    }

    /**
     * Bước 3: Gửi lại OTP (chống spam: 60s cooldown)
     */
    @PostMapping("/resend-otp")
    public SendOtpResponse resendOtp(@RequestParam Long userId) {
        return sendVerificationEmailUseCase.resend(userId);
    }

    // ================== LOGIN ==================

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return loginUseCase.execute(request);
    }

    @PostMapping("/logout")
    public void logout() {
        logoutUseCase.execute();
    }

    // ================== FORGOT & RESET PASSWORD ==================

    /**
     * Bước 1: Quên mật khẩu → Tạo và gửi OTP khôi phục về Email
     */
    @PostMapping("/forgot_password")
    public SendOtpResponse forgotPassword(@RequestParam String email) {
        return forgotPasswordUseCase.execute(email);
    }

    /**
     * Bước 2: Xác nhận OTP + Đặt lại mật khẩu mới
     */
    @PostMapping("/reset_password")
    public VerifyOtpResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return resetPasswordUseCase.execute(request);
    }

    /**
     * Bước 2 (riêng): Xác minh OTP quên mật khẩu (chỉ verify, chưa đổi mật khẩu)
     */
    @PostMapping("/verify-forgot-otp")
    public VerifyOtpResponse verifyForgotPasswordOtp(
            @Valid @RequestBody VerifyForgotPasswordOtpRequest request) {
        return otpService.verifyForgotPasswordOtp(request.getEmail(), request.getCode());
    }

    @PostMapping("/refresh_token")
    public LoginResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return refreshTokenUseCase.execute(request);
    }
}