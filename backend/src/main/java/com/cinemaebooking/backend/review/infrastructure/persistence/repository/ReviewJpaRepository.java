package com.cinemaebooking.backend.review.infrastructure.persistence.repository;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import com.cinemaebooking.backend.review.infrastructure.persistence.entity.ReviewJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewJpaRepository extends SoftDeleteJpaRepository<ReviewJpaEntity> {

    Optional<ReviewJpaEntity> findByUserIdAndMovieIdAndDeletedFalse(Long userId, Long movieId);

    Optional<ReviewJpaEntity> findByBookingIdAndDeletedFalse(Long bookingId);

    boolean existsByUserIdAndMovieIdAndDeletedFalse(Long userId, Long movieId);

    Page<ReviewJpaEntity> findByMovieIdAndDeletedFalse(Long movieId, Pageable pageable);

    Page<ReviewJpaEntity> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);
}
