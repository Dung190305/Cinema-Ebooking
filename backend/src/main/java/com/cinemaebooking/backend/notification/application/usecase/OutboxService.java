package com.cinemaebooking.backend.notification.application.usecase;

import com.cinemaebooking.backend.notification.application.dto.PaymentSuccessOutboxPayload;
import com.cinemaebooking.backend.notification.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.cinemaebooking.backend.notification.infrastructure.persistence.entity.OutboxEventJpaEntity.AggregateType;
import com.cinemaebooking.backend.notification.infrastructure.persistence.entity.OutboxEventJpaEntity.OutboxStatus;
import com.cinemaebooking.backend.notification.infrastructure.persistence.repository.OutboxJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Quản lý outbox events — ghi event vào DB trong cùng transaction với payment.
 * Email gửi thực tế do OutboxJob (scheduled) đọc từ bảng outbox và xử lý.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    public static final String EVENT_PAYMENT_SUCCESS = "PAYMENT_SUCCESS";

    private final OutboxJpaRepository outboxJpaRepository;
    private final ObjectMapper objectMapper;

    /**
     * Lưu outbox event trong cùng transaction với payment/booking.
     * Dùng REQUIRES_NEW để đảm bảo event được ghi ngay cả khi gọi từ nested transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePaymentSuccessEvent(PaymentSuccessOutboxPayload payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            OutboxEventJpaEntity event = OutboxEventJpaEntity.builder()
                    .aggregateType(AggregateType.BOOKING)
                    .aggregateId(payload.getBookingId())
                    .eventType(EVENT_PAYMENT_SUCCESS)
                    .payload(jsonPayload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();

            outboxJpaRepository.save(event);
            log.info("Outbox event saved: bookingId={}, bookingCode={}",
                    payload.getBookingId(), payload.getBookingCode());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox payload for bookingId={}: {}",
                    payload.getBookingId(), e.getMessage());
        }
    }
}
