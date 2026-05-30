package com.cinemaebooking.backend.user_coupon.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.user_coupon.domain.enums.UserCouponStatus;
import com.cinemaebooking.backend.user_coupon.domain.valueobject.UserCouponId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
public class UserCoupon extends BaseEntity<UserCouponId> {

    private Long userId;
    private Long couponId;
    private LocalDateTime receivedAt;
    private Integer usageRemain;
    private LocalDateTime usedAt;
    private LocalDateTime expiredAt;
    private UserCouponStatus status;

    /**
     * Factory method: tạo UserCoupon mới khi redeem.
     */
    public static UserCoupon redeem(Long userId, Long couponId,
                                    LocalDateTime receivedAt,
                                    Integer usageRemain,
                                    LocalDateTime expiredAt) {
        return UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .receivedAt(receivedAt)
                .usageRemain(usageRemain)
                .usedAt(null)
                .expiredAt(expiredAt)
                .status(UserCouponStatus.ACTIVE)
                .build();
    }

    // ========== BUSINESS METHODS ==========

    /**
     * Sử dụng coupon (giảm 1 lần dùng).
     */
    public void use() {
        if (this.status != UserCouponStatus.ACTIVE) {
            throw CommonExceptions.invalidInput(
                    "Cannot use coupon: status must be ACTIVE (current: " + this.status + ")");
        }
        if (this.usageRemain <= 0) {
            throw CommonExceptions.invalidInput("Cannot use coupon: no remaining usage");
        }
        this.usageRemain--;
        if (this.usageRemain == 0) {
            this.status = UserCouponStatus.USED;
            this.usedAt = LocalDateTime.now();
        }
    }

    /**
     * Hoàn tác một lần sử dụng (khi huỷ booking). Tăng usageRemain, nếu là USED và hết lượt thì quay về ACTIVE nếu chưa hết hạn.
     */
    public void restoreUsage(LocalDateTime now) {
        if (this.status == UserCouponStatus.EXPIRED) {
            throw CommonExceptions.invalidInput("Cannot restore an already expired coupon");
        }
        if (this.usageRemain == 0 && this.status == UserCouponStatus.USED) {
            if (this.expiredAt != null && this.expiredAt.isBefore(now)) {
                // Đã hết hạn → không thể khôi phục
                throw CommonExceptions.invalidInput(
                        "Cannot restore coupon: it has passed its expiry date");
            }
            this.status = UserCouponStatus.ACTIVE;
        }
        this.usageRemain++;
    }

    /**
     * Đánh dấu coupon đã hết hạn.
     */
    public void expire() {
        if (this.status == UserCouponStatus.ACTIVE) {
            this.status = UserCouponStatus.EXPIRED;
        }
    }

    /**
     * Admin thu hồi coupon
     */
    public void revoke() {
        if (this.status == UserCouponStatus.EXPIRED || this.status == UserCouponStatus.REVOKED) {
            throw CommonExceptions.invalidInput("Cannot revoke coupon: already " + this.status);
        }
        this.status = UserCouponStatus.REVOKED;
    }

    // Phương thức cũ giữ lại (không dùng trong use case mới)
    @Deprecated
    public void markAsUsed() {
        use();
    }

    /**
     * Kiểm tra coupon có thể sử dụng được hay không.
     */
    public boolean isUsable() {
        if (this.status != UserCouponStatus.ACTIVE) {
            return false;
        }

        if (this.usageRemain == null || this.usageRemain <= 0) {
            return false;
        }

        if (this.expiredAt != null && this.expiredAt.isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }
}