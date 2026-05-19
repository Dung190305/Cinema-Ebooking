package com.cinemaebooking.backend.review.infrastructure.adapter;

import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.common.exception.domain.ReviewExceptions;
import com.cinemaebooking.backend.review.application.port.ReviewRepository;
import com.cinemaebooking.backend.review.domain.model.Review;
import com.cinemaebooking.backend.review.domain.valueobject.ReviewId;
import com.cinemaebooking.backend.review.infrastructure.mapper.ReviewMapper;
import com.cinemaebooking.backend.review.infrastructure.persistence.entity.ReviewJpaEntity;
import com.cinemaebooking.backend.review.infrastructure.persistence.repository.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository jpaRepository;
    private final ReviewMapper mapper;
    private final BookingJpaRepository bookingJpaRepository;

    @Override
    @Transactional
    public Review save(Review review) {
        ReviewJpaEntity entity;

        if (review.getId() == null) {
            entity = mapper.toEntity(review);
            var bookingRef = bookingJpaRepository.getReferenceById(review.getBookingId());
            entity.setUser(bookingRef.getUser());
            entity.setBooking(bookingRef);
        } else {
            entity = jpaRepository.findById(review.getId().getValue())
                    .orElseThrow(() -> ReviewExceptions.notFound(review.getId()));
            mapper.updateEntity(review, entity);
        }

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findById(Long id) {
        return jpaRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findByUserIdAndMovieId(Long userId, Long movieId) {
        return jpaRepository.findByUserIdAndMovieIdAndDeletedFalse(userId, movieId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findByBookingId(Long bookingId) {
        return jpaRepository.findByBookingIdAndDeletedFalse(bookingId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndMovieId(Long userId, Long movieId) {
        return jpaRepository.existsByUserIdAndMovieIdAndDeletedFalse(userId, movieId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findByMovieId(Long movieId, Pageable pageable) {
        return jpaRepository.findByMovieIdAndDeletedFalse(movieId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserIdAndDeletedFalse(userId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void delete(Review review) {
        ReviewJpaEntity entity = jpaRepository.findById(review.getId().getValue())
                .orElseThrow(() -> ReviewExceptions.notFound(review.getId()));
        entity.softDelete();
        jpaRepository.save(entity);
    }
}
