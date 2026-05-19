package com.cinemaebooking.backend.coupon.infrastructure.adapter;

import com.cinemaebooking.backend.coupon.application.port.CouponRepository;
import com.cinemaebooking.backend.coupon.domain.model.Coupon;
import com.cinemaebooking.backend.coupon.domain.valueobject.CouponId;
import com.cinemaebooking.backend.coupon.infrastructure.mapper.CouponMapper;
import com.cinemaebooking.backend.coupon.infrastructure.persistence.entity.CouponJpaEntity;
import com.cinemaebooking.backend.coupon.infrastructure.persistence.repository.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository jpaRepository;
    private final CouponMapper mapper;

    @Override
    public Coupon create(Coupon coupon) {
        CouponJpaEntity entity = mapper.toEntity(coupon);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Coupon update(Coupon coupon) {

        CouponJpaEntity existing =
                jpaRepository.findByIdOrThrow(
                        coupon.getId().getValue()
                );

        existing.setUsageLimit(coupon.getUsageLimit());
        existing.setRemainingUsage(coupon.getRemainingUsage());
        existing.setEndDate(coupon.getEndDate());
        existing.setStatus(coupon.getStatus());

        return mapper.toDomain(
                jpaRepository.save(existing)
        );
    }

    @Override
    public Coupon updateDraft(Coupon coupon){
        CouponJpaEntity existing =
                jpaRepository.findByIdOrThrow(
                        coupon.getId().getValue()
                );
        existing.setCode(coupon.getCode());
        existing.setType(coupon.getType());
        existing.setValue(coupon.getValue());
        existing.setUsageLimit(coupon.getUsageLimit());
        existing.setRemainingUsage(coupon.getRemainingUsage());
        existing.setPerUserUsage(coupon.getPerUserUsage());
        existing.setMinimumBookingValue(coupon.getMinimumBookingValue());
        existing.setMaximumDiscountAmount(coupon.getMaximumDiscountAmount());
        existing.setEndDate(coupon.getEndDate());
        existing.setEndDate(coupon.getEndDate());
        existing.setStatus(coupon.getStatus());

        return mapper.toDomain(
                jpaRepository.save(existing)
        );
    }

    @Override
    public void updateStatus(Coupon coupon) {
        CouponJpaEntity existing =
                jpaRepository.findByIdOrThrow(
                        coupon.getId().getValue()
                );

        existing.setStatus(coupon.getStatus());
        jpaRepository.save(existing);
    }


    @Override
    public Optional<Coupon> findById(CouponId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Page<Coupon> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(CouponId id) {
        CouponJpaEntity entity = jpaRepository.findByIdOrThrow(id.getValue());
        jpaRepository.delete(entity);
    }

    @Override
    public boolean existsById(CouponId id) {
        return jpaRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCodeIgnoreCase(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, CouponId id) {
        return jpaRepository.existsByCodeIgnoreCaseAndIdNot(code, id.getValue());
    }
}