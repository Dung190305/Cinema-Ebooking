package com.cinemaebooking.backend.otp.infrastructure.persistence.entity;

import com.cinemaebooking.backend.infrastructure.persistence.entity.BaseJpaEntity;
import com.cinemaebooking.backend.otp.domain.model.OtpType;
import com.cinemaebooking.backend.user.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * OtpJpaEntity - Persistence model cho bảng otps.
 *
 * <p>Lưu trữ mã OTP dùng để xác minh email khi đăng ký tài khoản.
 *
 * <p>Design decisions:
 * <ul>
 *   <li>Mỗi user chỉ có tối đa 1 OTP active tại một thời điểm</li>
 *   <li>OTP có thời hạn 5 phút, sau đó tự động hết hiệu lực</li>
 *   <li>Tối đa 5 lần nhập sai — quá sẽ bị khóa</li>
 *   <li>Khi verify thành công, verified = true (không xóa để audit)</li>
 *   <li>Kế thừa BaseJpaEntity để có auditing và soft delete</li>
 * </ul>
 *
 * @author ducthinhn
 * @since 2026
 */
@Entity
@Table(
        name = "otps",
        indexes = {
                @Index(name = "idx_otps_user_id", columnList = "user_id"),
                @Index(name = "idx_otps_code", columnList = "code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class OtpJpaEntity extends BaseJpaEntity {

    /**
     * User sở hữu OTP.
     * Dùng LAZY fetch vì thường chỉ cần userId khi query.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    /**
     * Mã OTP 6 chữ số (ví dụ: "482917").
     */
    @NotBlank
    @Column(nullable = false, length = 6)
    private String code;

    /**
     * Loại OTP: EMAIL_VERIFICATION hoặc PASSWORD_RESET.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false, length = 20)
    private OtpType otpType;

    /**
     * Thời điểm OTP hết hiệu lực (mặc định 5 phút).
     */
    @NotNull
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    /**
     * Số lần user đã nhập sai OTP.
     */
    @NotNull
    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private int attempts = 0;

    /**
     * Cờ đánh dấu OTP đã được xác minh thành công.
     */
    @NotNull
    @Column(name = "verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Override
    protected void beforeSoftDelete() {}
}
