package com.cinemaebooking.backend.notification.application.port;

import com.cinemaebooking.backend.notification.domain.model.Notification;
import com.cinemaebooking.backend.notification.domain.valueObject.NotificationId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(NotificationId id);

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndUnread(Long userId, Pageable pageable);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, int limit);

    long countUnreadByUserId(Long userId);

    void markAsRead(NotificationId id);
}
