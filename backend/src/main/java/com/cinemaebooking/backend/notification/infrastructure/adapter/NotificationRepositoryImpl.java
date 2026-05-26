package com.cinemaebooking.backend.notification.infrastructure.adapter;

import com.cinemaebooking.backend.notification.application.port.NotificationRepository;
import com.cinemaebooking.backend.notification.domain.model.Notification;
import com.cinemaebooking.backend.notification.domain.valueObject.NotificationId;
import com.cinemaebooking.backend.notification.infrastructure.mapper.NotificationMapper;
import com.cinemaebooking.backend.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import com.cinemaebooking.backend.notification.infrastructure.persistence.repository.NotificationJpaRepository;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import com.cinemaebooking.backend.user.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper mapper;
    private final UserJpaRepository userJpaRepository;
    private final BookingJpaRepository bookingJpaRepository;
    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    @Transactional
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = mapper.toEntity(notification);
        entity.setUser(userJpaRepository.getReferenceById(notification.getUserId()));

        if (notification.getBookingId() != null) {
            entity.setBooking(bookingJpaRepository.getReferenceById(notification.getBookingId()));
        }
        if (notification.getPaymentId() != null) {
            entity.setPayment(paymentJpaRepository.getReferenceById(notification.getPaymentId()));
        }

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findById(NotificationId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findByUserIdAndUnread(Long userId, Pageable pageable) {
        return jpaRepository
                .findByUserIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(userId,
                        com.cinemaebooking.backend.notification.domain.enums.NotificationStatus.UNREAD,
                        pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, int limit) {
        return jpaRepository.findTop50ByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .limit(limit)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadByUserId(Long userId) {
        return jpaRepository.countByUserIdAndStatusAndDeletedFalse(
                userId,
                com.cinemaebooking.backend.notification.domain.enums.NotificationStatus.UNREAD);
    }

    @Override
    @Transactional
    public void markAsRead(NotificationId id) {
        jpaRepository.findById(id.getValue()).ifPresent(entity -> {
            entity.setStatus(com.cinemaebooking.backend.notification.domain.enums.NotificationStatus.READ);
        });
    }
}
