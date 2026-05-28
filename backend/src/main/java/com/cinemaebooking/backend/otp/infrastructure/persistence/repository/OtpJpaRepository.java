package com.cinemaebooking.backend.otp.infrastructure.persistence.repository;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import com.cinemaebooking.backend.otp.domain.model.OtpType;
import com.cinemaebooking.backend.otp.infrastructure.persistence.entity.OtpJpaEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * OtpJpaRepository - JPA Repository cho bảng otps.
 *
 * <p>Kế thừa SoftDeleteJpaRepository — tự động áp dụng
 * JPA @SQLRestriction cho soft delete trên mọi query.
 *
 * <p>Điều kiện "active" = deleted + verified + chưa hết hạn.
 * Việc check expiredAt nằm trong domain layer để tránh
 * timezone mismatch giữa MySQL CURRENT_TIMESTAMP và Java LocalDateTime.
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Repository
public interface OtpJpaRepository extends SoftDeleteJpaRepository<OtpJpaEntity> {

    /**
     * Tìm OTP chưa verify của một user theo loại.
     * Điều kiện expiredAt được check trong Otp domain (isExpired()).
     */
    @Query("""
            SELECT o FROM OtpJpaEntity o
            WHERE o.user.id = :userId
              AND o.otpType = :otpType
              AND o.verified = false
            """)
    Optional<OtpJpaEntity> findUnverifiedByUserIdAndType(Long userId, OtpType otpType);

    /**
     * Tìm OTP chưa verify của một user (bất kỳ loại nào).
     */
    @Query("""
            SELECT o FROM OtpJpaEntity o
            WHERE o.user.id = :userId
              AND o.verified = false
            """)
    Optional<OtpJpaEntity> findUnverifiedByUserId(Long userId);

    /**
     * Tìm OTP chưa verify bằng mã code.
     */
    @Query("""
            SELECT o FROM OtpJpaEntity o
            WHERE o.code = :code
              AND o.verified = false
            """)
    Optional<OtpJpaEntity> findUnverifiedByCode(String code);

    /**
     * Xóa (soft delete) tất cả OTP của một user với loại cụ thể.
     */
    @Modifying
    @Query("""
            UPDATE OtpJpaEntity o
            SET o.deleted = true, o.deletedAt = :now
            WHERE o.user.id = :userId
              AND o.otpType = :otpType
              AND o.deleted = false
            """)
    void deleteByUserIdAndOtpType(Long userId, OtpType otpType, LocalDateTime now);
}
