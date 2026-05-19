package com.cinemaebooking.backend.coupon.application.usecase;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.CouponExceptions;
import com.cinemaebooking.backend.coupon.application.dto.CouponResponse;
import com.cinemaebooking.backend.coupon.application.dto.UpdateDraftCouponRequest;
import com.cinemaebooking.backend.coupon.application.mapper.CouponResponseMapper;
import com.cinemaebooking.backend.coupon.application.port.CouponRepository;
import com.cinemaebooking.backend.coupon.application.validator.CouponCommandValidator;
import com.cinemaebooking.backend.coupon.domain.model.Coupon;
import com.cinemaebooking.backend.coupon.domain.valueobject.CouponId;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateDraftCouponUseCase {

    private final CouponRepository couponRepository;
    private final CouponResponseMapper mapper;
    private final CouponCommandValidator validator;

    public CouponResponse execute(CouponId id, UpdateDraftCouponRequest request) {
        validator.validateUpdateDraftRequest(id, request);
        Coupon coupon = loadCoupon(id);

        coupon.updateDraft(
                request.getCode(),
                request.getType(),
                request.getValue(),
                request.getUsageLimit(),
                request.getPerUserUsage(),
                request.getPointsToRedeem(),
                request.getMinimumBookingValue(),
                request.getMaximumDiscountAmount(),
                request.getStartDate(),
                request.getEndDate()
        );

        try {
            Coupon saved = couponRepository.updateDraft(coupon);
            return mapper.toResponse(saved);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException ex) {
            throw CommonExceptions.concurrencyConflict();
        }
    }

    private Coupon loadCoupon(CouponId id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> CouponExceptions.notFound(id));
    }
}