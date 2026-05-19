package com.cinemaebooking.backend.payment.infrastructure.adapter;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.common.exception.domain.PaymentExceptions;
import com.cinemaebooking.backend.payment.application.port.PaymentRepository;
import com.cinemaebooking.backend.payment.domain.model.Payment;
import com.cinemaebooking.backend.payment.infrastructure.mapper.PaymentMapper;
import com.cinemaebooking.backend.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.cinemaebooking.backend.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpa;
    private final BookingJpaRepository bookingJpaRepository;
    private final PaymentMapper mapper;

    @Override
    public Payment save(Payment payment) {
        BookingJpaEntity booking = bookingJpaRepository.getReferenceById(payment.getBookingId());
        if (booking.getId() == null) {
            throw new IllegalStateException("Invalid booking reference");
        }

        return jpa.findByPaymentCode(payment.getPaymentCode())
                .map(existing -> {
                    // UPDATE — apply all mutable fields onto managed entity
                    existing.setStatus(payment.getStatus());
                    existing.setTransactionId(payment.getTransactionId());
                    existing.setProviderResponse(payment.getProviderResponse());
                    existing.setPaidAt(payment.getPaidAt());
                    existing.setAmount(payment.getAmount());
                    existing.setMethod(payment.getMethod());
                    existing.setExpiredAt(payment.getExpiredAt());
                    return mapper.toDomain(jpa.save(existing));
                })
                .orElseGet(() -> {
                    // INSERT — domain chưa có trong DB
                    PaymentJpaEntity entity = PaymentJpaEntity.builder()
                            .booking(booking)
                            .amount(payment.getAmount())
                            .method(payment.getMethod())
                            .status(payment.getStatus())
                            .paymentCode(payment.getPaymentCode())
                            .expiredAt(payment.getExpiredAt())
                            .transactionId(payment.getTransactionId())
                            .providerResponse(payment.getProviderResponse())
                            .paidAt(payment.getPaidAt())
                            .build();
                    return mapper.toDomain(jpa.save(entity));
                });
    }

    @Override
    public Payment findByPaymentCode(String paymentCode) {
        return jpa.findByPaymentCode(paymentCode)
                .map(mapper::toDomain)
                .orElseThrow(() -> PaymentExceptions.notFound(paymentCode));
    }

    @Override
    public void update(Payment payment) {
        PaymentJpaEntity entity = jpa.findByPaymentCode(payment.getPaymentCode())
                .orElseThrow(() -> PaymentExceptions.notFound(payment.getPaymentCode()));

        mapper.updateEntity(payment, entity);
        jpa.save(entity);
    }
}
