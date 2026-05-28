package com.cinemaebooking.backend.otp.application.port;

import com.cinemaebooking.backend.otp.application.dto.RegisterWithOtpRequest;
import com.cinemaebooking.backend.otp.application.dto.SendOtpResponse;
import com.cinemaebooking.backend.otp.application.dto.VerifyOtpResponse;

public interface OtpService {
    SendOtpResponse registerAndSendOtp(RegisterWithOtpRequest request);
    SendOtpResponse resendOtp(Long userId);
    VerifyOtpResponse verifyOtp(String code, Long userId);
    SendOtpResponse forgotPasswordAndSendOtp(String email);
    VerifyOtpResponse verifyForgotPasswordOtpAndReset(String email, String code, String newPassword);
    VerifyOtpResponse verifyForgotPasswordOtp(String email, String code);
}
