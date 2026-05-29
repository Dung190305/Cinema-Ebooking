package com.cinemaebooking.backend.notification.infrastructure.persistence.entity;

import com.cinemaebooking.backend.infrastructure.persistence.entity.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(name = "idx_outbox_status_created", columnList = "status, created_at"),
                @Index(name = "idx_outbox_aggregate", columnList = "aggregate_type, aggregate_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class OutboxEventJpaEntity extends BaseJpaEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private AggregateType aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "processed_at")
    private Instant processedAt;

    public enum AggregateType {
        BOOKING,
        PAYMENT
    }

    public enum OutboxStatus {
        PENDING,
        SENT,
        FAILED
    }

    public void markSent() {
        this.status = OutboxStatus.SENT;
        this.processedAt = Instant.now();
    }

    public void markFailed(String error) {
        this.retryCount = this.retryCount + 1;
        this.lastError = error;
        if (this.retryCount >= 3) {
            this.status = OutboxStatus.FAILED;
            this.processedAt = Instant.now();
        }
    }
}
