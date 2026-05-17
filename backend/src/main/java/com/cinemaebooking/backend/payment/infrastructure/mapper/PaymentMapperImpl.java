package com.cinemaebooking.backend.payment.infrastructure.mapper;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.payment.domain.model.Payment;
import com.cinemaebooking.backend.payment.domain.valueObject.PaymentId;
import com.cinemaebooking.backend.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import com.cinemaebooking.backend.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentMapperImpl implements PaymentMapper {

    private final BookingJpaRepository bookingJpaRepository;
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment toDomain(PaymentJpaEntity e) {
        if (e == null) return null;

        Payment domain = Payment.builder()
                .paymentCode(e.getPaymentCode())
                .bookingId(e.getBooking() != null ? e.getBooking().getId() : null)
                .amount(e.getAmount())
                .method(e.getMethod())
                .status(e.getStatus())
                .transactionId(e.getTransactionId())
                .providerResponse(e.getProviderResponse())
                .paidAt(e.getPaidAt())
                .expiredAt(e.getExpiredAt())
                .build();
        // Assign id after build using Unsafe to bypass Lombok's field hiding
        assignIdFromEntity(domain, e.getId());
        return domain;
    }

    @Override
    public PaymentJpaEntity toEntity(Payment domain) {
        if (domain.getBookingId() == null) {
            throw new IllegalStateException("bookingId cannot be null");
        }

        BookingJpaEntity booking =
                bookingJpaRepository.getReferenceById(domain.getBookingId());

        if (booking.getId() == null) {
            throw new IllegalStateException("Invalid booking reference");
        }

        PaymentJpaEntity entity = paymentJpaRepository
                .findByPaymentCode(domain.getPaymentCode())
                .orElse(null);

        if (entity == null) {
            // INSERT
            entity = PaymentJpaEntity.builder()
                    .booking(booking)
                    .amount(domain.getAmount())
                    .method(domain.getMethod())
                    .status(domain.getStatus())
                    .paymentCode(domain.getPaymentCode())
                    .expiredAt(domain.getExpiredAt())
                    .transactionId(domain.getTransactionId())
                    .providerResponse(domain.getProviderResponse())
                    .paidAt(domain.getPaidAt())
                    .build();
        } else {
            // UPDATE
            entity.setStatus(domain.getStatus());
            entity.setTransactionId(domain.getTransactionId());
            entity.setProviderResponse(domain.getProviderResponse());
            entity.setPaidAt(domain.getPaidAt());
            entity.setAmount(domain.getAmount());
            entity.setMethod(domain.getMethod());
            entity.setExpiredAt(domain.getExpiredAt());
        }

        return paymentJpaRepository.save(entity);
    }

    @Override
    public void updateEntity(Payment d, PaymentJpaEntity e) {
        e.setStatus(d.getStatus());
        e.setTransactionId(d.getTransactionId());
        e.setProviderResponse(d.getProviderResponse());
        e.setPaidAt(d.getPaidAt());
    }

    /**
     * Uses Unsafe to bypass Lombok's @SuperBuilder field-hiding.
     * This is safe because Payment has @SuperBuilder and id is protected.
     */
    @SuppressWarnings("restriction")
    private void assignIdFromEntity(Payment domain, Long id) {
        try {
            var unsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafe.setAccessible(true);
            sun.misc.Unsafe u = (sun.misc.Unsafe) unsafe.get(null);
            var field = Payment.class.getSuperclass().getDeclaredField("id");
            u.putObjectVolatile(domain, u.objectFieldOffset(field), PaymentId.of(id));
        } catch (Exception ex) {
            throw new RuntimeException("Cannot assign id to Payment domain", ex);
        }
    }
}
