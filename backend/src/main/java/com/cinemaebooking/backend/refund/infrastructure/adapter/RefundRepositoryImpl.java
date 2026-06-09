package com.cinemaebooking.backend.refund.infrastructure.adapter;

import com.cinemaebooking.backend.common.exception.domain.RefundExceptions;
import com.cinemaebooking.backend.refund.application.port.RefundRepository;
import com.cinemaebooking.backend.refund.domain.enums.RefundStatus;
import com.cinemaebooking.backend.refund.domain.model.Refund;
import com.cinemaebooking.backend.refund.domain.valueobject.RefundId;
import com.cinemaebooking.backend.refund.infrastructure.mapper.RefundMapper;
import com.cinemaebooking.backend.refund.infrastructure.persistence.entity.RefundJpaEntity;
import com.cinemaebooking.backend.refund.infrastructure.persistence.repository.RefundJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefundRepositoryImpl implements RefundRepository {

    private final RefundJpaRepository jpaRepository;
    private final RefundMapper mapper;

    @Override
    public Refund create(Refund refund) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(refund)));
    }

    @Override
    public Refund update(Refund refund) {
        RefundJpaEntity entity = jpaRepository.findById(refund.getId().getValue())
                .orElseThrow(() -> RefundExceptions.notFound(refund.getId()));

        mapper.updateEntity(entity, refund);

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Refund> findById(RefundId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Refund> findByBookingId(Long bookingId) {
        return jpaRepository.findByBooking_IdAndDeletedFalse(bookingId)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Refund> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Refund> findAll(RefundStatus status, Pageable pageable) {
        return jpaRepository.findAllWithBooking(status, pageable)
                .map(mapper::toDomainWithBookingDetails);
    }

    @Override
    public boolean existsByBookingId(Long bookingId) {
        return jpaRepository.existsByBooking_IdAndDeletedFalse(bookingId);
    }
}
