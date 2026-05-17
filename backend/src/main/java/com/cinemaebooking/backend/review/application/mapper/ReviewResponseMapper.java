package com.cinemaebooking.backend.review.application.mapper;

import com.cinemaebooking.backend.review.application.dto.ReviewResponse;
import com.cinemaebooking.backend.review.domain.model.Review;

public interface ReviewResponseMapper {
    ReviewResponse toResponse(Review review);
}
