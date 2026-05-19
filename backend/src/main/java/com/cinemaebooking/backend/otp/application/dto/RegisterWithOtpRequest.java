package com.cinemaebooking.backend.otp.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * RegisterWithOtpRequest - Request để đăng ký tài khoản mới với xác minh OTP.
 *
 * <p>Đây là DTO trung gian được dùng trong OtpService.registerAndSendOtp().
 * Các trường tương ứng với RegisterRequest gốc trong AuthDTO.
 *
 * <p>Note: dateOfBirth và gender dùng String thay vì LocalDate/UserGender
 * để tránh phụ thuộc vào các type cụ thể từ user domain.
 * Việc convert sang đúng type được thực hiện trong RegisterUseCase hoặc OtpService.
 *
 * @author ducthinhn
 * @since 2026
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterWithOtpRequest {

    /** Họ tên đầy đủ của user (bắt buộc). */
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    /** Email của user (bắt buộc, phải unique). */
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    /** Mật khẩu (bắt buộc, tối thiểu 6 ký tự). */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    /** Số điện thoại (tùy chọn). */
    private String phoneNumber;

    /** Ngày sinh (tùy chọn, format: yyyy-MM-dd). */
    private String dateOfBirth;

    /** Giới tính (tùy chọn: MALE, FEMALE, OTHER). */
    private String gender;
}
