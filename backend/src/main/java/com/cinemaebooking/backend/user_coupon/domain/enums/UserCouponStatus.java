package com.cinemaebooking.backend.user_coupon.domain.enums;

/**
 * UserCouponStatus - Represents the lifecycle status of a coupon claimed by a user.
 * Responsibility:
 * - Track the current state of user-coupon relationship
 * - Support business rules for usage and expiration
 *
 * @author Hieu Nguyen
 * @since 2026
 */
public enum UserCouponStatus {

    ACTIVE,

    USED,

    EXPIRED,

    REVOKED
}