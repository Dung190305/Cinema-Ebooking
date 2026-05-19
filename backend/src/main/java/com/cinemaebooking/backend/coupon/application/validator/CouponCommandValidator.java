package com.cinemaebooking.backend.coupon.application.validator;

import com.cinemaebooking.backend.common.exception.ErrorCategory;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.CouponExceptions;
import com.cinemaebooking.backend.common.validation.engine.ValidationEngine;
import com.cinemaebooking.backend.common.validation.factory.ValidationFactory;
import com.cinemaebooking.backend.coupon.application.dto.CouponFullUpdateRequest;
import com.cinemaebooking.backend.coupon.application.dto.CreateCouponRequest;
import com.cinemaebooking.backend.coupon.application.dto.UpdateCouponRequest;
import com.cinemaebooking.backend.coupon.application.dto.UpdateDraftCouponRequest;
import com.cinemaebooking.backend.coupon.application.port.CouponRepository;
import com.cinemaebooking.backend.coupon.domain.valueobject.CouponId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CouponCommandValidator {

    private final CouponRepository couponRepository;

    // ================== CREATE ==================
    public void validateCreateRequest(CreateCouponRequest request) {
        if (request == null) {
            throw CommonExceptions.invalidInput("Request must not be null");
        }

        validateFields(request, null);

        validateDateRange(
                request.getStartDate(),
                request.getEndDate()
        );
    }

    // ================== UPDATE ==================
    public void validateUpdateRequest(CouponId couponId,
                                      UpdateCouponRequest request) {

        validateCouponId(couponId);

        if (request == null) {
            throw CommonExceptions.invalidInput("Request must not be null");
        }

        var coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> CouponExceptions.notFound(couponId));

        validateFields(request, couponId);

        validateUsageLimitForUpdate(
                request.getUsageLimit(),
                coupon.getUsageLimit(),
                coupon.getRemainingUsage()
        );

        validateEndDateForUpdate(
                request.getEndDate(),
                coupon.getStartDate()
        );
    }

    public void validateUpdateDraftRequest(CouponId couponId,UpdateDraftCouponRequest request) {

        validateCouponId(couponId);

        if (request == null) {
            throw CommonExceptions.invalidInput("Request must not be null");
        }

        var coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> CouponExceptions.notFound(couponId));

        validateFields(request, couponId);

        validateDateRange(
                request.getStartDate(),
                request.getEndDate()
        );
    }

    public void validateCouponId(CouponId couponId) {
        if (couponId == null) {
            throw CommonExceptions.invalidInput("Coupon id must not be null");
        }
    }

    // ================== FIELD VALIDATION ==================
    private void validateFields(Object request, CouponId couponId) {
        var profile = ValidationFactory.coupon();

        ValidationEngine engine = ValidationEngine.of();

        if (request instanceof CouponFullUpdateRequest req) {

            engine
                    .validate(
                            req.getCode(),
                            "code",
                            profile.codeRules()
                    )

                    .validate(
                            req.getUsageLimit(),
                            "usageLimit",
                            profile.usageLimitRules()
                    )

                    .validate(
                            req.getPerUserUsage(),
                            "perUserUsage",
                            profile.perUserUsageRules()
                    )

                    .validate(
                            req.getPointsToRedeem(),
                            "pointsToRedeem",
                            profile.pointsToRedeemRules()
                    )

                    .validate(
                            req.getValue(),
                            "value",
                            profile.valueRules()
                    )

                    .validate(
                            req.getMinimumBookingValue(),
                            "minimumBookingValue",
                            profile.minimumBookingValueRules()
                    )

                    .validate(
                            req.getMaximumDiscountAmount(),
                            "maximumDiscountAmount",
                            profile.maximumDiscountAmountRules()
                    )

                    .validate(
                            req.getStartDate(),
                            "startDate",
                            profile.startDateRules()
                    )

                    .validate(
                            req.getEndDate(),
                            "endDate",
                            profile.endDateRules()
                    );
        }

        if (request instanceof UpdateCouponRequest req) {

            engine
                    .validate(
                            req.getUsageLimit(),
                            "usageLimit",
                            profile.usageLimitRules()
                    )

                    .validate(
                            req.getEndDate(),
                            "endDate",
                            profile.endDateRules()
                    );
        }

        engine.throwIfInvalid();


        if (request instanceof CreateCouponRequest req) {
            String normalizeCode = normalize(req.getCode());
            engine.validateUnique(
                    normalizeCode,
                    "code",
                    value -> codeExists(normalizeCode))
                    .throwIfInvalid();
        }

        if (request instanceof UpdateDraftCouponRequest req) {
            String normalizeCode = normalize(req.getCode());
            engine.validateUnique(
                            normalizeCode,
                            "code",
                            value -> codeExistsAndIdNot(normalizeCode,couponId))
                    .throwIfInvalid();
        }
    }

    // ================== UPDATE VALIDATION ==================
    private void validateUsageLimitForUpdate(Integer newUsageLimit,
                                             Integer currentUsageLimit,
                                             Integer remainingUsage) {

        if (newUsageLimit == null || newUsageLimit <= 0) {
            throw CommonExceptions.invalidInput(
                    "usageLimit",ErrorCategory.INVALID_VALUE,
                    "Giới hạn sử dụng phải lớn hơn 0"
            );
        }

        int usedCount = currentUsageLimit - remainingUsage;

        if (newUsageLimit < usedCount) {
            throw CommonExceptions.invalidInput(
                    "usageLimit",ErrorCategory.INVALID_VALUE,
                    "Giới hạn sử dụng không thể nhỏ hơn số lượng đã sử dụng"
            );
        }
    }

    private void validateEndDateForUpdate(java.time.LocalDate endDate,
                                          java.time.LocalDate startDate) {

        if (endDate == null) {
            throw CommonExceptions.invalidInput("endDate", ErrorCategory.REQUIRED,"Ngày kết thúc không thể để trống"
            );
        }

        if (endDate.isBefore(java.time.LocalDate.now())) {
            throw CommonExceptions.invalidInput("endDate", ErrorCategory.INVALID_VALUE,"Ngày kết thúc phải sau ngày hôm nay"
            );
        }

        if (endDate.isBefore(startDate)) {
            throw CommonExceptions.invalidInput(
                    "endDate",ErrorCategory.INVALID_VALUE,"Ngày kết thúc phải sau ngày bắt đầu"
            );
        }
    }


    private void validateDateRange(LocalDate startDate,
                                   LocalDate endDate) {

        if (startDate != null
                && endDate != null
                && endDate.isBefore(startDate)) {

            throw CommonExceptions.invalidInput(
                    "endDate",
                    ErrorCategory.INVALID_VALUE,
                    "Ngày kết thúc phải sau ngày bắt đầu"
            );
        }
    }

    // ================== DUPLICATE CHECKS ==================
    private boolean codeExists(String code) {

        if (code == null) {
            return false;
        }

        return couponRepository.existsByCode(code);
    }

    private boolean codeExistsAndIdNot(String code, CouponId couponId){

        if (code == null || couponId == null) {
            return false;
        }

        return couponRepository.existsByCodeAndIdNot(code, couponId);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}