package com.cinemaebooking.backend.review.application.port;

import com.cinemaebooking.backend.review.application.dto.ReviewResponse;
import com.cinemaebooking.backend.review.domain.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(Long id);

    Optional<Review> findByUserIdAndMovieId(Long userId, Long movieId);

    Optional<Review> findByBookingId(Long bookingId);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    Page<ReviewResponse> findByMovieId(Long movieId, Pageable pageable);

    Page<ReviewResponse> findByUserId(Long userId, Pageable pageable);

    void delete(Review review);
}
