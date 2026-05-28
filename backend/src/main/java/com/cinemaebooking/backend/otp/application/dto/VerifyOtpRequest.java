package com.cinemaebooking.backend.otp.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpRequest {

    /**
     * Mã OTP 6 chữ số do user nhập vào.
     * Validation @NotBlank đảm bảo không được để trống.
     */
    @NotBlank(message = "Mã OTP không được để trống")
    private String code;

    /**
     * ID của user đang thực hiện verify.
     * Frontend lấy từ response của register-otp (SendOtpResponse.userId).
     */
    private Long userId;
}
