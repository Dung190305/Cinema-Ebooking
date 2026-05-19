package com.cinemaebooking.backend.coupon.application.usecase;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.CouponExceptions;
import com.cinemaebooking.backend.coupon.application.dto.CouponResponse;
import com.cinemaebooking.backend.coupon.application.dto.UpdateCouponRequest;
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
public class UpdateCouponUseCase {

    private final CouponRepository couponRepository;
    private final CouponResponseMapper mapper;
    private final CouponCommandValidator validator;

    public CouponResponse execute(CouponId id, UpdateCouponRequest request) {
        validator.validateUpdateRequest(id, request);
        Coupon coupon = loadCoupon(id);
        coupon.update(
                request.getUsageLimit(),
                request.getEndDate()
        );

        try {
            Coupon saved = couponRepository.update(coupon);
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