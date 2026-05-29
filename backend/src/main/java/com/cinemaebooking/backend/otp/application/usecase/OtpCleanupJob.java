package com.cinemaebooking.backend.otp.application.usecase;

import com.cinemaebooking.backend.loyalty.infrastructure.persistence.repository.LoyaltyAccountJpaRepository;
import com.cinemaebooking.backend.otp.domain.model.OtpType;
import com.cinemaebooking.backend.otp.infrastructure.persistence.repository.OtpJpaRepository;
import com.cinemaebooking.backend.user.domain.enums.UserStatus;
import com.cinemaebooking.backend.user.infrastructure.persistence.entity.UserJpaEntity;
import com.cinemaebooking.backend.user.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OtpCleanupJob - Dọn dẹp dữ liệu OTP và user không hợp lệ định kỳ.
 *
 * <p>Chạy mỗi 1 phút, thực hiện 2 nhiệm vụ:
 * <ol>
 *   <li>Soft-delete các OTP EMAIL_VERIFICATION đã hết hạn
 *       (dùng JPQL LOCALTIMESTAMP để tránh lệch timezone giữa JVM và MySQL)</li>
 *   <li>Xóa user INACTIVE đã tạo > 15 phút trước VÀ không còn OTP EMAIL_VERIFICATION active nào
 *       (user không verify được coi như từ bỏ, có thể đăng ký lại)</li>
 * </ol>
 *
 * <p>Lưu ý: Không xóa PASSWORD_RESET OTP — user đang quên mật khẩu có thể đợi lâu hơn.
 *
 * @author ai-agent
 * @since 2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OtpCleanupJob {

    private final OtpJpaRepository otpJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final LoyaltyAccountJpaRepository loyaltyAccountJpaRepository;

    /**
     * Chạy mỗi 1 phút để cleanup expired OTP và unverified users.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cleanup() {
        log.debug("[OtpCleanupJob] Bắt đầu cleanup...");
        deleteExpiredOtps();
        deleteUnverifiedUsers();
        log.debug("[OtpCleanupJob] Cleanup hoàn tất.");
    }

    // ─── OTP cleanup ───────────────────────────────────────────

    /**
     * Soft-delete tất cả OTP EMAIL_VERIFICATION đã hết hạn.
     *
     * <p>Dùng JPQL với LOCALTIMESTAMP (lấy từ JVM/Hibernate, không phải MySQL NOW())
     * để đảm bảo so sánh thời gian chính xác, tránh lệch timezone gây xóa nhầm.
     */
    private void deleteExpiredOtps() {
        int deleted = otpJpaRepository.deleteExpiredByType(OtpType.EMAIL_VERIFICATION);
        if (deleted > 0) {
            log.info("[OtpCleanupJob] Đã xóa {} OTP EMAIL_VERIFICATION đã hết hạn", deleted);
        }
    }

    // ─── User cleanup ──────────────────────────────────────────

    /**
     * Xóa user INACTIVE đã tạo từ lâu và không còn OTP EMAIL_VERIFICATION active nào.
     *
     * <p>Điều kiện xóa:
     * <ul>
     *   <li>User có status = INACTIVE</li>
     *   <li>Đã tạo tài khoản cách đây > 15 phút (grace period — tránh xóa user vừa đăng ký)</li>
     *   <li>Không có OTP EMAIL_VERIFICATION active nào (chưa verify)</li>
     * </ul>
     */
    private void deleteUnverifiedUsers() {
        // Lấy user INACTIVE đã tạo > 15 phút trước, chưa bị xóa
        List<UserJpaEntity> candidates = userJpaRepository
                .findAll().stream()
                .filter(u -> !u.isDeleted())
                .filter(u -> u.getStatus() == UserStatus.INACTIVE)
                .filter(u -> u.getCreatedAt() != null
                        && u.getCreatedAt().isBefore(java.time.LocalDateTime.now().minusMinutes(15)))
                .toList();

        // Với mỗi candidate, kiểm tra còn OTP active không
        for (UserJpaEntity user : candidates) {
            boolean hasActiveOtp = otpJpaRepository
                    .findAll().stream()
                    .filter(o -> !o.isDeleted())
                    .filter(o -> o.getUser().getId().equals(user.getId()))
                    .filter(o -> o.getOtpType() == OtpType.EMAIL_VERIFICATION)
                    .filter(o -> !o.isVerified())
                    .filter(o -> o.getExpiredAt() != null && o.getExpiredAt().isAfter(java.time.LocalDateTime.now()))
                    .findFirst()
                    .isPresent();

            if (hasActiveOtp) {
                continue; // Còn OTP active → không xóa
            }

            // Không còn OTP active → xóa loyalty account trước (FK), rồi xóa user
            loyaltyAccountJpaRepository.findByUserId(user.getId())
                    .ifPresent(loyalty -> {
                        loyaltyAccountJpaRepository.softDeleteById(loyalty.getId());
                        log.info("[OtpCleanupJob] Đã xóa loyalty account: {}", user.getEmail());
                    });

            userJpaRepository.deleteById(user.getId());
            log.info("[OtpCleanupJob] Đã xóa user INACTIVE: {}", user.getEmail());
        }
    }
}
