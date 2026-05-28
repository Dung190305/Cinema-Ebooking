package com.cinemaebooking.backend.notification.infrastructure.mapper;

import com.cinemaebooking.backend.infrastructure.mapper.BaseMapper;
import com.cinemaebooking.backend.notification.domain.model.Notification;
import com.cinemaebooking.backend.notification.infrastructure.persistence.entity.NotificationJpaEntity;

public interface NotificationMapper extends BaseMapper<Notification, NotificationJpaEntity> {
}
