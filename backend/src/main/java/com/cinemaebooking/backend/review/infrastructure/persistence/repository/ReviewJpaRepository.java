package com.cinemaebooking.backend.review.infrastructure.persistence.repository;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepository;
import com.cinemaebooking.backend.review.domain.enums.ReviewStatus;
import com.cinemaebooking.backend.review.infrastructure.persistence.entity.ReviewJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewJpaRepository extends SoftDeleteJpaRepository<ReviewJpaEntity> {

    Optional<ReviewJpaEntity> findByUserIdAndMovieIdAndDeletedFalse(Long userId, Long movieId);

    Optional<ReviewJpaEntity> findByUserIdAndMovieIdAndDeletedFalseAndStatus(Long userId, Long movieId, ReviewStatus status);

    Optional<ReviewJpaEntity> findByBookingIdAndDeletedFalse(Long bookingId);

    boolean existsByUserIdAndMovieIdAndDeletedFalse(Long userId, Long movieId);

    @EntityGraph(attributePaths = {"user", "booking"})
    @Query("SELECT r FROM ReviewJpaEntity r WHERE r.movieId = :movieId AND r.status = :status AND r.deleted = false")
    Page<ReviewJpaEntity> findActiveByMovieId(
            @org.springframework.data.repository.query.Param("movieId") Long movieId,
            @org.springframework.data.repository.query.Param("status") ReviewStatus status,
            Pageable pageable
    );

    // User reviews: bao gồm cả HIDDEN để user tự quản lý
    @EntityGraph(attributePaths = {"user", "booking"})
    Page<ReviewJpaEntity> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);
}
