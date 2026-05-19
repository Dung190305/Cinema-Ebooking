package com.cinemaebooking.backend.otp.infrastructure.mapper;

import com.cinemaebooking.backend.otp.domain.model.Otp;
import com.cinemaebooking.backend.otp.domain.valueObject.OtpId;
import com.cinemaebooking.backend.otp.infrastructure.persistence.entity.OtpJpaEntity;
import com.cinemaebooking.backend.user.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * OtpMapperImpl - Implementation của OtpMapper.
 *
 * <p>Chuyển đổi giữa Otp (domain model) và OtpJpaEntity (persistence).
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class OtpMapperImpl implements OtpMapper {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Otp toDomain(OtpJpaEntity e) {
        if (e == null) return null;

        return Otp.builder()
                .id(OtpId.ofNullable(e.getId()))
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .code(e.getCode())
                .otpType(e.getOtpType())
                .expiredAt(e.getExpiredAt())
                .createdAt(e.getCreatedAt())
                .attempts(e.getAttempts())
                .verified(e.isVerified())
                .build();
    }

    @Override
    public OtpJpaEntity toEntity(Otp domain) {
        if (domain == null) return null;

        return OtpJpaEntity.builder()
                .user(userJpaRepository.getReferenceById(domain.getUserId()))
                .code(domain.getCode())
                .otpType(domain.getOtpType())
                .expiredAt(domain.getExpiredAt())
                .attempts(domain.getAttempts())
                .verified(domain.isVerified())
                .build();
    }

    @Override
    public void updateEntity(Otp domain, OtpJpaEntity entity) {
        entity.setCode(domain.getCode());
        entity.setOtpType(domain.getOtpType());
        entity.setExpiredAt(domain.getExpiredAt());
        entity.setAttempts(domain.getAttempts());
        entity.setVerified(domain.isVerified());
    }
}
