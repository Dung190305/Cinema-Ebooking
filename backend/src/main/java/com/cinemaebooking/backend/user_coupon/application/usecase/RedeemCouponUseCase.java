package com.cinemaebooking.backend.user_coupon.application.usecase;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.UserCouponExceptions;
import com.cinemaebooking.backend.coupon.application.port.CouponRepository;
import com.cinemaebooking.backend.loyalty.application.usecase.transactional.DeductPointsUseCase;
import com.cinemaebooking.backend.loyalty.domain.enums.LoyaltyTransactionType;
import com.cinemaebooking.backend.user_coupon.application.dto.RedeemCouponRequest;
import com.cinemaebooking.backend.user_coupon.application.dto.UserCouponResponse;
import com.cinemaebooking.backend.user_coupon.application.mapper.UserCouponResponseMapper;
import com.cinemaebooking.backend.user_coupon.application.port.CouponPort;
import com.cinemaebooking.backend.user_coupon.application.port.UserCouponRepository;
import com.cinemaebooking.backend.user_coupon.application.validator.RedeemCouponValidator;
import com.cinemaebooking.backend.user_coupon.domain.model.UserCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RedeemCouponUseCase {

    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final CouponPort couponPort;
    private final UserCouponResponseMapper mapper;
    private final RedeemCouponValidator validator;
    private final DeductPointsUseCase deductPointsUseCase;

    @Transactional
    public UserCouponResponse execute(RedeemCouponRequest request) {
        validator.validate(request);

        var coupon = couponPort.findValidCoupon(request.getCouponId(), LocalDateTime.now())
                .orElseThrow(); // already validated

        UserCoupon userCoupon = UserCoupon.redeem(
                request.getUserId(),
                request.getCouponId(),
                LocalDateTime.now(),
                coupon.perUserUsage(),
                coupon.expiryDate()
        );

        UserCoupon saved = userCouponRepository.create(userCoupon);

        if (coupon.pointsToRedeem() > 0) {
            deductPointsUseCase.execute(
                    request.getUserId(),
                    BigDecimal.valueOf(coupon.pointsToRedeem()),
                    LoyaltyTransactionType.REDEEM_FOR_COUPON,
                    saved.getId().getValue()
            );
        }

        return mapper.toResponse(saved);
    }
}