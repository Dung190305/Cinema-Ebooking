package com.cinemaebooking.backend.coupon.application.usecase;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.CouponExceptions;
import com.cinemaebooking.backend.coupon.application.port.CouponRepository;
import com.cinemaebooking.backend.coupon.domain.model.Coupon;
import com.cinemaebooking.backend.coupon.domain.valueobject.CouponId;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivateCouponUseCase {

    private final CouponRepository couponRepository;

    public void execute(CouponId id) {

        Coupon coupon = loadCoupon(id);

        coupon.activate();

        try {
            couponRepository.updateStatus(coupon);

        } catch (OptimisticLockException
                 | ObjectOptimisticLockingFailureException ex) {

            throw CommonExceptions.concurrencyConflict();
        }
    }

    private Coupon loadCoupon(CouponId id) {

        if (id == null) {
            throw CommonExceptions.invalidInput(
                    "couponId must not be null"
            );
        }

        return couponRepository.findById(id)
                .orElseThrow(() -> CouponExceptions.notFound(id));
    }
}