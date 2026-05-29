package com.cinemaebooking.backend.user_coupon.infrastructure.adapter;

import com.cinemaebooking.backend.user_coupon.application.port.UserCouponRepository;
import com.cinemaebooking.backend.user_coupon.domain.model.UserCoupon;
import com.cinemaebooking.backend.user_coupon.domain.valueobject.UserCouponId;
import com.cinemaebooking.backend.user_coupon.infrastructure.mapper.UserCouponMapper;
import com.cinemaebooking.backend.user_coupon.infrastructure.persistence.entity.UserCouponJpaEntity;
import com.cinemaebooking.backend.user_coupon.infrastructure.persistence.repository.UserCouponJpaRepository;
import com.cinemaebooking.backend.user_coupon.domain.enums.UserCouponStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private final UserCouponJpaRepository jpaRepository;
    private final UserCouponMapper mapper;

    @Override
    public UserCoupon create(UserCoupon userCoupon) {
        UserCouponJpaEntity entity = mapper.toEntity(userCoupon);
        entity = jpaRepository.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public UserCoupon update(UserCoupon userCoupon) {
        UserCouponJpaEntity entity = jpaRepository.findByIdOrThrow(userCoupon.getId().getValue());
        mapper.updateEntity(entity, userCoupon);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<UserCoupon> findById(UserCouponId id) {
        return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId) {
        return jpaRepository.findByUserIdAndCouponId(userId, couponId).map(mapper::toDomain);
    }

    @Override
    public Page<UserCoupon> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<UserCoupon> findByUserIdAndStatus(Long userId, String status, Pageable pageable) {
        UserCouponStatus couponStatus = UserCouponStatus.valueOf(status);
        return jpaRepository.findByUserIdAndStatus(userId, couponStatus, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<UserCoupon> findAvailableExpiredBefore(LocalDateTime dateTime) {
        return jpaRepository.findByStatusAndExpiredAtBefore(UserCouponStatus.ACTIVE, dateTime)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIdAndCouponId(Long userId, Long couponId) {
        return jpaRepository.existsByUserIdAndCouponId(userId, couponId);
    }

    @Override
    public boolean existsById(UserCouponId id) {
        return jpaRepository.existsById(id.getValue());
    }

    @Override
    public void deleteById(UserCouponId id) {
        jpaRepository.findByIdOrThrow(id.getValue());
        jpaRepository.deleteById(id.getValue()); // soft delete thừa kế
    }

    @Override
    public Optional<UserCoupon> findByUserIdAndCode(Long userId, String code) {
        return jpaRepository.findByUserIdAndCodeWithCoupon(userId, code)
                .map(mapper::toDomain);
    }

    @Override
    public void updateStatus(Long userCouponId, UserCouponStatus status) {
        UserCouponJpaEntity entity = jpaRepository.findByIdOrThrow(userCouponId);
        entity.setStatus(status);
        entity.setUsageRemain(entity.getUsageRemain() + 1);
        entity.setUsedAt(null);
        jpaRepository.save(entity);
    }
}