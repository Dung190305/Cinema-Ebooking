package com.cinemaebooking.backend.otp.application.port;

import com.cinemaebooking.backend.otp.domain.model.Otp;
import com.cinemaebooking.backend.otp.domain.model.OtpType;

import java.util.Optional;

/**
 * OtpRepository - Port interface cho OTP persistence operations.
 *
 * <p>Định nghĩa các operations cần thiết để quản lý OTP data.
 * Implementation: OtpRepositoryImpl.
 *
 * @author ducthinhn
 * @since 2026
 */
public interface OtpRepository {

    /**
     * Lưu hoặc cập nhật OTP.
     * Dùng cho cả INSERT (tạo OTP mới) và UPDATE (cập nhật attempts/verified).
     */
    Otp save(Otp otp);

    /**
     * Tìm OTP active (chưa xóa, chưa verified, chưa hết hạn)
     * của một user với loại OTP cụ thể.
     *
     * @param userId  ID của user
     * @param otpType loại OTP
     * @return Optional chứa Otp domain nếu tìm thấy
     */
    Optional<Otp> findActiveByUserIdAndType(Long userId, OtpType otpType);

    /**
     * Tìm OTP active của một user (bất kỳ loại nào).
     */
    Optional<Otp> findActiveByUserId(Long userId);

    /**
     * Tìm OTP active bằng mã code.
     */
    Optional<Otp> findActiveByCode(String code);

    /**
     * Xóa tất cả OTP của một user với loại cụ thể.
     * Dùng khi resend OTP — xóa OTP cũ trước khi tạo OTP mới.
     */
    void deleteByUserIdAndType(Long userId, OtpType otpType);

    /**
     * Xóa một OTP theo ID.
     */
    void deleteById(Long id);
}
