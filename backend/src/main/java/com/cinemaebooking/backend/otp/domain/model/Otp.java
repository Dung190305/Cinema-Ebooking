package com.cinemaebooking.backend.otp.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.otp.domain.valueObject.OtpId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Otp - Domain model đại diện cho một mã OTP.
 *
 * <p>Entity này quản lý vòng đời của mã OTP từ lúc sinh ra đến khi verify
 * hoặc hết hạn. Chứa business logic kiểm tra trạng thái OTP.
 *
 * <p>Design decisions:
 * <ul>
 *   <li>Kế thừa BaseEntity<OtpId> theo convention của project</li>
 *   <li>Immutable value cho các field cố định (code, otpType, expiredAt)</li>
 *   <li>Muttable cho attempts, verified (thay đổi trong quá trình verify)</li>
 *   <li>Mã OTP không được hash vì chỉ sống 5 phút</li>
 * </ul>
 *
 * @author ducthinhn
 * @since 2026
 */
@Getter
@SuperBuilder(toBuilder = true)
public class Otp extends BaseEntity<OtpId> {

    private Long userId;
    private String code;
    private OtpType otpType;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private int attempts;
    private boolean verified;

    public static final int MAX_ATTEMPTS = 5;

    // =======================================================================
    // DOMAIN LOGIC
    // =======================================================================

    /**
     * Kiểm tra OTP đã hết hạn chưa.
     *
     * @return true nếu thời điểm hiện tại vượt quá expiredAt
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    /**
     * Kiểm tra user đã nhập sai quá số lần cho phép chưa.
     *
     * @return true nếu attempts >= MAX_ATTEMPTS
     */
    public boolean isMaxAttemptsReached() {
        return attempts >= MAX_ATTEMPTS;
    }

    /**
     * Kiểm tra OTP có hợp lệ để verify không.
     *
     * <p>OTP hợp lệ khi:
     * <ul>
     *   <li>Chưa hết hạn</li>
     *   <li>Chưa được verify</li>
     *   <li>Chưa vượt số lần sai tối đa</li>
     *   <li>Mã code khớp với input</li>
     * </ul>
     *
     * @param inputCode mã OTP user nhập vào
     * @return true nếu OTP hợp lệ
     */
    public boolean isValid(String inputCode) {
        return !isExpired() && !verified && !isMaxAttemptsReached() && code.equals(inputCode);
    }

    /**
     * Đánh dấu OTP đã được verify thành công.
     */
    public void markAsVerified() {
        this.verified = true;
    }

    /**
     * Tăng số lần nhập sai OTP.
     * Mỗi lần user nhập sai, gọi method này.
     */
    public void incrementAttempts() {
        this.attempts++;
    }
}
