package com.cinemaebooking.backend.notification.infrastructure.persistence.repository;

import com.cinemaebooking.backend.notification.domain.enums.NotificationStatus;
import com.cinemaebooking.backend.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {

    Page<NotificationJpaEntity> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<NotificationJpaEntity> findByUserIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            Long userId, NotificationStatus status, Pageable pageable);

    List<NotificationJpaEntity> findTop50ByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndStatusAndDeletedFalse(Long userId, NotificationStatus status);
}
