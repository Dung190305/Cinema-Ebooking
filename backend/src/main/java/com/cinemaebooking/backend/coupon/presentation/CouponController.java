package com.cinemaebooking.backend.coupon.presentation;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.coupon.application.dto.CouponResponse;
import com.cinemaebooking.backend.coupon.application.dto.CreateCouponRequest;
import com.cinemaebooking.backend.coupon.application.dto.UpdateCouponRequest;
import com.cinemaebooking.backend.coupon.application.dto.UpdateDraftCouponRequest;
import com.cinemaebooking.backend.coupon.application.usecase.*;
import com.cinemaebooking.backend.coupon.domain.valueobject.CouponId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CreateCouponUseCase createCouponUseCase;
    private final UpdateCouponUseCase updateCouponUseCase;
    private final UpdateDraftCouponUseCase updateDraftCouponUseCase;
    private final DeleteCouponUseCase deleteCouponUseCase;
    private final GetCouponDetailUseCase getCouponDetailUseCase;
    private final GetCouponListUseCase getCouponListUseCase;
    private final ActivateCouponUseCase activateCouponUseCase;
    private final DisableCouponUseCase disableCouponUseCase;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse createCoupon(@RequestBody CreateCouponRequest request) {
        return createCouponUseCase.execute(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CouponResponse updateCoupon(@PathVariable Long id,
                                       @RequestBody UpdateCouponRequest request) {
        CouponId couponId = toCouponId(id);
        return updateCouponUseCase.execute(couponId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/draft")
    public CouponResponse updateDraftCoupon(@PathVariable Long id,
                                            @RequestBody UpdateDraftCouponRequest request) {
        CouponId couponId = toCouponId(id);
        return updateDraftCouponUseCase.execute(couponId, request);
    }

    /*@PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCoupon(@PathVariable Long id) {
        CouponId couponId = toCouponId(id);
        deleteCouponUseCase.execute(couponId);
    }*/

    @GetMapping("/{id}")
    public CouponResponse getCouponDetail(@PathVariable Long id) {
        CouponId couponId = toCouponId(id);
        return getCouponDetailUseCase.execute(couponId);
    }

    @GetMapping
    public Page<CouponResponse> getCouponList(@PageableDefault(size = 10) Pageable pageable) {
        return getCouponListUseCase.execute(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public void activateCoupon(@PathVariable Long id) {
        CouponId couponId = toCouponId(id);
        activateCouponUseCase.execute(couponId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/disable")
    public void disableCoupon(@PathVariable Long id) {
        CouponId couponId = toCouponId(id);
        disableCouponUseCase.execute(couponId);
    }

    private CouponId toCouponId(Long id) {
        if (id == null) {
            throw CommonExceptions.invalidInput("Coupon id must not be null");
        }
        return CouponId.of(id);
    }
}