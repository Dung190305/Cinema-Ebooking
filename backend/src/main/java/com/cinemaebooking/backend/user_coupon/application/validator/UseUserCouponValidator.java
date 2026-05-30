package com.cinemaebooking.backend.user_coupon.application.validator;

import com.cinemaebooking.backend.common.exception.ErrorDetail;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.UserCouponExceptions;
import com.cinemaebooking.backend.common.validation.engine.ValidationContext;
import com.cinemaebooking.backend.common.validation.engine.ValidationRule;
import com.cinemaebooking.backend.common.validation.factory.ValidationFactory;
import com.cinemaebooking.backend.user_coupon.application.dto.UseUserCouponRequest;
import com.cinemaebooking.backend.user_coupon.application.port.UserCouponRepository;
import com.cinemaebooking.backend.user_coupon.domain.enums.UserCouponStatus;
import com.cinemaebooking.backend.user_coupon.domain.valueobject.UserCouponId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UseUserCouponValidator {

    private final UserCouponRepository userCouponRepository;

    public void validate(UseUserCouponRequest request) {
        if (request == null) {
            throw CommonExceptions.invalidInput("Use request must not be null");
        }

        var profile = ValidationFactory.userCoupon();
        executeRules(request.getUserCouponId(), "userCouponId", profile.userCouponIdRules());
        executeRules(request.getUserId(), "userId", profile.userIdRules());

        UserCouponId id = UserCouponId.of(request.getUserCouponId());
        var userCoupon = userCouponRepository.findById(id)
                .orElseThrow(() -> UserCouponExceptions.notFound(id));

        if (!userCoupon.getUserId().equals(request.getUserId())) {
            throw UserCouponExceptions.notOwnedByUser(id, request.getUserId());
        }

        if (userCoupon.getStatus() != UserCouponStatus.ACTIVE) {
            throw UserCouponExceptions.notAvailable(id);
        }

        if (userCoupon.getExpiredAt() != null && userCoupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            userCoupon.expire();
            userCouponRepository.update(userCoupon);
            throw UserCouponExceptions.couponExpired(userCoupon.getCouponId());
        }

        if (userCoupon.getUsageRemain() <= 0) {
            throw UserCouponExceptions.noRemainingUsage(id);
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