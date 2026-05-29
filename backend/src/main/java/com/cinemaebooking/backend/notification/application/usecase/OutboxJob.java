package com.cinemaebooking.backend.notification.application.usecase;

import com.cinemaebooking.backend.notification.application.dto.BookingEmailData;
import com.cinemaebooking.backend.notification.application.dto.PaymentSuccessOutboxPayload;
import com.cinemaebooking.backend.notification.application.port.BookingEmailService;
import com.cinemaebooking.backend.notification.application.port.QRCodeService;
import com.cinemaebooking.backend.notification.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.cinemaebooking.backend.notification.infrastructure.persistence.entity.OutboxEventJpaEntity.OutboxStatus;
import com.cinemaebooking.backend.notification.infrastructure.persistence.repository.OutboxJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;

/**
 * Scheduled job đọc pending outbox events và gửi email.
 *
 * <p>Poll mỗi 10 giây, lấy tối đa 20 event PENDING.
 * Nếu gửi fail → tăng retryCount. Retry tối đa 3 lần, sau đó đánh FAILED.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxJob {

    private static final int BATCH_SIZE = 20;
    private static final Set<String> PAYMENT_SUCCESS_EVENTS = Set.of(OutboxService.EVENT_PAYMENT_SUCCESS);

    private final OutboxJpaRepository outboxJpaRepository;
    private final BookingEmailService bookingEmailService;
    private final QRCodeService qrCodeService;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelay = 10_000, initialDelay = 5_000)
    public void processOutboxEvents() {
        List<OutboxEventJpaEntity> events = outboxJpaRepository
                .findPendingEvents(OutboxStatus.PENDING, PageRequest.of(0, BATCH_SIZE));

        if (events.isEmpty()) {
            return;
        }

        log.info("[OutboxJob] Processing {} pending events", events.size());

        for (OutboxEventJpaEntity event : events) {
            try {
                processEventInNewTransaction(event);
            } catch (Exception e) {
                log.error("[OutboxJob] Unexpected error processing event id={}: {}",
                        event.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Chạy trong transaction riêng — mỗi event xử lý độc lập.
     * Dùng TransactionTemplate thay vì @Transactional annotation để tránh self-invocation proxy bypass.
     */
    public void processEventInNewTransaction(OutboxEventJpaEntity event) {
        transactionTemplate.executeWithoutResult(status -> {
            if (event.getStatus() != OutboxStatus.PENDING) {
                return;
            }

            String eventType = event.getEventType();

            if (PAYMENT_SUCCESS_EVENTS.contains(eventType)) {
                processPaymentSuccessEvent(event);
            } else {
                log.warn("[OutboxJob] Unknown event type: {}", eventType);
                event.markFailed("Unknown event type: " + eventType);
                outboxJpaRepository.save(event);
            }
        });
    }

    private void processPaymentSuccessEvent(OutboxEventJpaEntity event) {
        try {
            PaymentSuccessOutboxPayload payload = objectMapper.readValue(
                    event.getPayload(), PaymentSuccessOutboxPayload.class);

            log.info("[OutboxJob] Processing event id={}, bookingCode={}, email={}",
                    event.getId(), payload.getBookingCode(), payload.getUserEmail());

            if (payload.getBookingCode() == null || payload.getBookingCode().isBlank()) {
                event.markFailed("bookingCode is null or blank in payload");
                outboxJpaRepository.save(event);
                log.error("[OutboxJob] Invalid payload: bookingCode is null/blank, event id={}", event.getId());
                return;
            }

            String qrBase64 = qrCodeService.generateQRCodeBase64(payload.getBookingCode(), 300, 300);
            log.info("[OutboxJob] QR generated for bookingCode={}, length={}",
                    payload.getBookingCode(), qrBase64 != null ? qrBase64.length() : "null");

            if (qrBase64 == null || qrBase64.isBlank()) {
                event.markFailed("QR code generation returned null/blank");
                outboxJpaRepository.save(event);
                log.error("[OutboxJob] QR code generation failed for bookingId={}", payload.getBookingId());
                return;
            }

            BookingEmailData emailData = mapToBookingEmailData(payload);

            bookingEmailService.sendPaymentSuccessEmail(
                    payload.getUserEmail(),
                    payload.getUserName(),
                    emailData,
                    qrBase64
            );

            event.markSent();
            outboxJpaRepository.save(event);
            log.info("[OutboxJob] Payment success email sent for bookingId={}", payload.getBookingId());

        } catch (JsonProcessingException e) {
            event.markFailed("Invalid JSON payload: " + e.getMessage());
            outboxJpaRepository.save(event);
        } catch (Exception e) {
            event.markFailed(e.getMessage());
            outboxJpaRepository.save(event);
            log.error("[OutboxJob] Failed to send payment email for event id={}: {}",
                    event.getId(), e.getMessage());
        }
    }

    private BookingEmailData mapToBookingEmailData(PaymentSuccessOutboxPayload p) {
        BookingEmailData.BookingEmailDataBuilder builder = BookingEmailData.builder()
                .bookingId(p.getBookingId())
                .bookingCode(p.getBookingCode())
                .userId(p.getUserId())
                .movieTitle(p.getMovieTitle())
                .cinemaName(p.getCinemaName())
                .roomName(p.getRoomName())
                .showtimeStartTime(p.getShowtimeStartTime())
                .finalAmount(p.getFinalAmount());

        if (p.getSeats() != null && !p.getSeats().isEmpty()) {
            builder.seats(p.getSeats().stream()
                    .map(s -> BookingEmailData.SeatData.builder()
                            .seatName(s.getSeatName())
                            .seatType(s.getSeatType())
                            .price(s.getPrice())
                            .build())
                    .toList());
        }

        if (p.getCombos() != null && !p.getCombos().isEmpty()) {
            builder.combos(p.getCombos().stream()
                    .map(c -> BookingEmailData.ComboData.builder()
                            .comboName(c.getComboName())
                            .quantity(c.getQuantity())
                            .unitPrice(c.getUnitPrice())
                            .build())
                    .toList());
        }

        if (p.getCoupon() != null) {
            builder.coupon(BookingEmailData.CouponData.builder()
                    .code(p.getCoupon().getCode())
                    .discountAmount(p.getCoupon().getDiscountAmount())
                    .build());
        }

        return builder.build();
    }
}
