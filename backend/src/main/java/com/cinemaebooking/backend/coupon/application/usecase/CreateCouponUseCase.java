package com.cinemaebooking.backend.coupon.application.usecase;

import com.cinemaebooking.backend.coupon.application.dto.CouponResponse;
import com.cinemaebooking.backend.coupon.application.dto.CreateCouponRequest;
import com.cinemaebooking.backend.coupon.application.mapper.CouponResponseMapper;
import com.cinemaebooking.backend.coupon.application.port.CouponRepository;
import com.cinemaebooking.backend.coupon.application.validator.CouponCommandValidator;
import com.cinemaebooking.backend.coupon.domain.enums.CouponStatus;
import com.cinemaebooking.backend.coupon.domain.model.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateCouponUseCase {

    private final CouponRepository couponRepository;
    private final CouponResponseMapper mapper;
    private final CouponCommandValidator validator;

    public CouponResponse execute(CreateCouponRequest request) {
        validator.validateCreateRequest(request);

        Coupon coupon = buildCoupon(request);
        Coupon saved = couponRepository.create(coupon);
        return mapper.toResponse(saved);
    }

    private Coupon buildCoupon(CreateCouponRequest request) {
        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .type(request.getType())
                .value(request.getValue())
                .usageLimit(request.getUsageLimit())
                .remainingUsage(request.getUsageLimit())
                .perUserUsage(request.getPerUserUsage())
                .pointsToRedeem(request.getPointsToRedeem())
                .minimumBookingValue(request.getMinimumBookingValue())
                .maximumDiscountAmount(request.getMaximumDiscountAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(CouponStatus.DRAFT)
                .build();
        return coupon;
    }
}