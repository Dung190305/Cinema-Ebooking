package com.cinemaebooking.backend.otp.infrastructure.mapper;

import com.cinemaebooking.backend.infrastructure.mapper.BaseMapper;
import com.cinemaebooking.backend.otp.domain.model.Otp;
import com.cinemaebooking.backend.otp.infrastructure.persistence.entity.OtpJpaEntity;
public interface OtpMapper extends BaseMapper<Otp, OtpJpaEntity> {
    void updateEntity(Otp domain, OtpJpaEntity entity);
}
