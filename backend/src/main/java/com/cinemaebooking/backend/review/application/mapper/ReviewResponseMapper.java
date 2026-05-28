package com.cinemaebooking.backend.review.application.mapper;

import com.cinemaebooking.backend.review.application.dto.ReviewResponse;
import com.cinemaebooking.backend.review.domain.model.Review;
import com.cinemaebooking.backend.review.infrastructure.persistence.entity.ReviewJpaEntity;

public interface ReviewResponseMapper {
    ReviewResponse toResponse(Review review);
    ReviewResponse toResponseFromEntity(ReviewJpaEntity entity);
}
