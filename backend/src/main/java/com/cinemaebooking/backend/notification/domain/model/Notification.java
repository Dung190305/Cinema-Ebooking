package com.cinemaebooking.backend.notification.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.notification.domain.enums.NotificationStatus;
import com.cinemaebooking.backend.notification.domain.enums.NotificationType;
import com.cinemaebooking.backend.notification.domain.valueObject.NotificationId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public class Notification extends BaseEntity<NotificationId> {

    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private Long bookingId;
    private Long paymentId;

    public static Notification create(
            Long userId,
            String title,
            String message,
            NotificationType type,
            Long bookingId,
            Long paymentId
    ) {
        return Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .status(NotificationStatus.UNREAD)
                .bookingId(bookingId)
                .paymentId(paymentId)
                .build();
    }

    public void markAsRead() {
        if (this.status == NotificationStatus.UNREAD) {
            this.status = NotificationStatus.READ;
        }
    }
}
