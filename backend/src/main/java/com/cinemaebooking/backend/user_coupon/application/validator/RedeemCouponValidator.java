package com.cinemaebooking.backend.user_coupon.application.validator;

import com.cinemaebooking.backend.common.exception.ErrorDetail;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.UserCouponExceptions;
import com.cinemaebooking.backend.common.validation.engine.ValidationContext;
import com.cinemaebooking.backend.common.validation.engine.ValidationRule;
import com.cinemaebooking.backend.common.validation.factory.ValidationFactory;
import com.cinemaebooking.backend.user_coupon.application.dto.RedeemCouponRequest;
import com.cinemaebooking.backend.user_coupon.application.port.CouponPort;
import com.cinemaebooking.backend.user_coupon.application.port.LoyaltyPort;
import com.cinemaebooking.backend.user_coupon.application.port.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedeemCouponValidator {

    private final UserCouponRepository userCouponRepository;
    private final CouponPort couponPort;
    private final LoyaltyPort loyaltyPort;

    public void validate(RedeemCouponRequest request) {
        if (request == null) {
            throw CommonExceptions.invalidInput("Redeem request must not be null");
        }

        // ========== FORMAT VALIDATION ==========
        var profile = ValidationFactory.userCoupon();
        executeRules(request.getUserId(), "userId", profile.userIdRules());

        Long userId = request.getUserId();
        String couponCode = request.getCouponCode();

        // ========== BUSINESS RULES ==========
        LocalDateTime now = LocalDateTime.now();
        var snapshot = couponPort.findValidCoupon(couponCode, now);
        Long couponId = snapshot.id();

        int pointsRequired = snapshot.pointsToRedeem();
        if (pointsRequired > 0) {
            int userPoints = loyaltyPort.getUserPoints(userId);
            if (userPoints < pointsRequired) {
                throw UserCouponExceptions.insufficientPoints(userId, pointsRequired, userPoints);
            }
        }

        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw UserCouponExceptions.alreadyExists(userId, couponId);
        }
    }

    private <T> void executeRules(T value, String fieldName, List<ValidationRule<T>> rules) {
        ValidationContext<T> context = new ValidationContext<>(value, fieldName);
        for (ValidationRule<T> rule : rules) {
            Optional<ErrorDetail> errorOpt = rule.validate(context);
            if (errorOpt.isPresent()) {
                ErrorDetail error = errorOpt.get();
                throw CommonExceptions.invalidInput(
                        error.getField() + ": " + error.getReason()
                );
            }
        }
    }
}