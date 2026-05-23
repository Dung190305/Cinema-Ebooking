package com.cinemaebooking.backend.otp.infrastructure.adapter;

import com.cinemaebooking.backend.otp.application.port.OtpRepository;
import com.cinemaebooking.backend.otp.domain.model.Otp;
import com.cinemaebooking.backend.otp.domain.model.OtpType;
import com.cinemaebooking.backend.otp.infrastructure.mapper.OtpMapper;
import com.cinemaebooking.backend.otp.infrastructure.persistence.entity.OtpJpaEntity;
import com.cinemaebooking.backend.otp.infrastructure.persistence.repository.OtpJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * OtpRepositoryImpl - Implementation của OtpRepository port.
 *
 * <p>Chuyển đổi giữa domain model (Otp) và JPA entity (OtpJpaEntity)
 * thông qua OtpMapper, giữ clean architecture rõ ràng.
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class OtpRepositoryImpl implements OtpRepository {

    private final OtpJpaRepository jpa;
    private final OtpMapper mapper;

    @Override
    public Otp save(Otp otp) {
        OtpJpaEntity entity = mapper.toEntity(otp);
        OtpJpaEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Otp> findActiveByUserIdAndType(Long userId, OtpType otpType) {
        return jpa.findUnverifiedByUserIdAndType(userId, otpType)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Otp> findActiveByUserId(Long userId) {
        return jpa.findUnverifiedByUserId(userId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Otp> findActiveByCode(String code) {
        return jpa.findUnverifiedByCode(code)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteByUserIdAndType(Long userId, OtpType otpType) {
        jpa.deleteByUserIdAndOtpType(userId, otpType, LocalDateTime.now());
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
