package com.cinemaebooking.backend.payment.infrastructure.persistence.repository;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import com.cinemaebooking.backend.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.cinemaebooking.backend.payment.domain.enums.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends SoftDeleteJpaRepository<PaymentJpaEntity> {
    Optional<PaymentJpaEntity> findByPaymentCode(String paymentCode);

    List<PaymentJpaEntity> findAllByStatusAndDeletedFalse(PaymentStatus status);
}