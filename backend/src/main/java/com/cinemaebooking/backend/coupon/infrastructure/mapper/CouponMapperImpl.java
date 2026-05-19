package com.cinemaebooking.backend.coupon.infrastructure.mapper;

import com.cinemaebooking.backend.coupon.domain.model.Coupon;
import com.cinemaebooking.backend.coupon.domain.valueobject.CouponId;
import com.cinemaebooking.backend.coupon.infrastructure.persistence.entity.CouponJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CouponMapperImpl implements CouponMapper {

    @Override
    public CouponJpaEntity toEntity(Coupon domain) {
        if (domain == null) return null;
        return CouponJpaEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .code(domain.getCode())
                .type(domain.getType())
                .value(domain.getValue())
                .usageLimit(domain.getUsageLimit())
                .remainingUsage(domain.getRemainingUsage())
                .perUserUsage(domain.getPerUserUsage())
                .pointsToRedeem(domain.getPointsToRedeem())
                .minimumBookingValue(domain.getMinimumBookingValue())
                .maximumDiscountAmount(domain.getMaximumDiscountAmount())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .status(domain.getStatus())
                .build();
    }

    @Override
    public Coupon toDomain(CouponJpaEntity entity) {
        if (entity == null) return null;
        return Coupon.builder()
                .id(CouponId.ofNullable(entity.getId()))
                .code(entity.getCode())
                .type(entity.getType())
                .value(entity.getValue())
                .usageLimit(entity.getUsageLimit())
                .remainingUsage(entity.getRemainingUsage())
                .perUserUsage(entity.getPerUserUsage())
                .pointsToRedeem(entity.getPointsToRedeem())
                .minimumBookingValue(entity.getMinimumBookingValue())
                .maximumDiscountAmount(entity.getMaximumDiscountAmount())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .build();
    }

    @Override
    public void updateEntity(CouponJpaEntity entity, Coupon domain) {
        if (entity == null || domain == null) return;
        entity.setCode(domain.getCode());
        entity.setType(domain.getType());
        entity.setValue(domain.getValue());
        entity.setUsageLimit(domain.getUsageLimit());
        entity.setRemainingUsage(domain.getRemainingUsage());
        entity.setPerUserUsage(domain.getPerUserUsage());
        entity.setPointsToRedeem(domain.getPointsToRedeem());
        entity.setMinimumBookingValue(domain.getMinimumBookingValue());
        entity.setMaximumDiscountAmount(domain.getMaximumDiscountAmount());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
    }
}