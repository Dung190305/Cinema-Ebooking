package com.cinemaebooking.backend.review.application.mapper;

import com.cinemaebooking.backend.review.application.dto.ReviewResponse;
import com.cinemaebooking.backend.review.domain.model.Review;
import com.cinemaebooking.backend.user.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReviewResponseMapperImpl implements ReviewResponseMapper {

    private final UserJpaRepository userJpaRepository;

    @Override
    public ReviewResponse toResponse(Review review) {
        if (review == null) return null;

        String userName = userJpaRepository.findById(review.getUserId())
                .map(u -> u.getFullName())
                .orElse(null);

        return ReviewResponse.builder()
                .reviewId(review.getId() != null ? review.getId().getValue() : null)
                .userId(review.getUserId())
                .userName(userName)
                .movieId(review.getMovieId())
                .bookingId(review.getBookingId())
                .rating(review.getRating())
                .comment(review.getComment())
                .sentiment(review.getSentiment())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .editedAt(review.getEditedAt())
                .edited(review.getEditedAt() != null)
                .build();
    }
}
