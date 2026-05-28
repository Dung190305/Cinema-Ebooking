package com.cinemaebooking.backend.review.application.mapper;

import com.cinemaebooking.backend.review.application.dto.ReviewResponse;
import com.cinemaebooking.backend.review.domain.model.Review;
import com.cinemaebooking.backend.review.infrastructure.persistence.entity.ReviewJpaEntity;
import com.cinemaebooking.backend.review.infrastructure.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewResponseMapperImpl implements ReviewResponseMapper {

    private final ReviewMapper mapper;

    @Override
    public ReviewResponse toResponse(Review review) {
        if (review == null) return null;

        return ReviewResponse.builder()
                .reviewId(review.getId() != null ? review.getId().getValue() : null)
                .userId(review.getUserId())
                .userName(null)  // caller should use toResponseFromEntity for userName
                .movieId(review.getMovieId())
                .bookingId(review.getBookingId())
                .rating(review.getRating())
                .comment(review.getComment())
                .finalText(review.getDisplayText())
                .sentiment(review.getSentiment())
                .decision(review.getDecision())
                .status(review.getStatus())
                .isSpoiler(review.isSpoiler())
                .spoilerConf(review.getSpoilerConf())
                .createdAt(review.getCreatedAt())
                .editedAt(review.getEditedAt())
                .edited(review.getEditedAt() != null)
                .build();
    }

    @Override
    public ReviewResponse toResponseFromEntity(ReviewJpaEntity entity) {
        if (entity == null) return null;

        return ReviewResponse.builder()
                .reviewId(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ? entity.getUser().getFullName() : null)
                .movieId(entity.getMovieId())
                .bookingId(entity.getBooking() != null ? entity.getBooking().getId() : null)
                .rating(entity.getRating())
                .comment(entity.getComment())
                .finalText(entity.getFinalText() != null ? entity.getFinalText() : entity.getComment())
                .sentiment(entity.getSentiment())
                .decision(entity.getDecision())
                .status(entity.getStatus())
                .isSpoiler(entity.isSpoiler())
                .spoilerConf(entity.getSpoilerConf())
                .createdAt(entity.getCreatedAt())
                .editedAt(entity.getEditedAt())
                .edited(entity.getEditedAt() != null)
                .build();
    }
}
