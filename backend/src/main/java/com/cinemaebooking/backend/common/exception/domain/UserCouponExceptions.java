package com.cinemaebooking.backend.common.exception.domain;

import com.cinemaebooking.backend.common.exception.BaseException;
import com.cinemaebooking.backend.common.exception.ErrorCode;
import com.cinemaebooking.backend.user_coupon.domain.valueobject.UserCouponId;

public final class UserCouponExceptions {

    private UserCouponExceptions() {}

    public static BaseException notFound(UserCouponId id) {
        return new BaseException(ErrorCode.USER_COUPON_NOT_FOUND,
                "User coupon not found with id: " + id);
    }

    public static BaseException alreadyExists(Long userId, Long couponId) {
        return new BaseException(ErrorCode.USER_COUPON_ALREADY_EXISTS,
                "User " + userId + " already owns coupon " + couponId);
    }

    public static BaseException couponNotFound(Long couponId) {
        return new BaseException(ErrorCode.COUPON_NOT_FOUND,
                "Coupon not found: " + couponId);
    }

    public static BaseException couponNotActive(Long couponId) {
        return new BaseException(ErrorCode.COUPON_INVALID,
                "Coupon " + couponId + " is not active");
    }

    public static BaseException couponNotActive(String code) {
        return new BaseException(ErrorCode.COUPON_INVALID,
                "Coupon " + code + " is not active");
    }

    public static BaseException couponExpired(Long couponId) {
        return new BaseException(ErrorCode.COUPON_EXPIRED,
                "Coupon " + couponId + " has expired");
    }

    public static BaseException couponExpired(String code) {
        return new BaseException(ErrorCode.COUPON_EXPIRED,
                "Coupon " + code + " has expired");
    }

    public static BaseException insufficientPoints(Long userId, int required, int available) {
        return new BaseException(ErrorCode.LOYALTY_INSUFFICIENT_POINTS,
                "User " + userId + " has insufficient points (need: " + required + ", have: " + available + ")");
    }

    public static BaseException notOwnedByUser(UserCouponId id, Long userId) {
        return new BaseException(ErrorCode.USER_COUPON_NOT_FOUND,
                "User coupon " + id + " does not belong to user " + userId);
    }

    public static BaseException notAvailable(UserCouponId id) {
        return new BaseException(ErrorCode.USER_COUPON_EXPIRED,
                "User coupon " + id + " is not available for use");
    }

    public static BaseException noRemainingUsage(UserCouponId id) {
        return new BaseException(ErrorCode.USER_COUPON_ALREADY_USED,
                "User coupon " + id + " has no remaining usage");
    }
}