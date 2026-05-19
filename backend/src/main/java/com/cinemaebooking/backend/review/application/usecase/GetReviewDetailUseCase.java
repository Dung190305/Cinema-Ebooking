package com.cinemaebooking.backend.review.application.usecase;

import com.cinemaebooking.backend.common.exception.domain.ReviewExceptions;
import com.cinemaebooking.backend.review.application.dto.ReviewResponse;
import com.cinemaebooking.backend.review.application.mapper.ReviewResponseMapper;
import com.cinemaebooking.backend.review.application.port.ReviewRepository;
import com.cinemaebooking.backend.review.domain.model.Review;
import com.cinemaebooking.backend.review.domain.valueobject.ReviewId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetReviewDetailUseCase {

    private final ReviewRepository reviewRepository;
    private final ReviewResponseMapper mapper;

    @Transactional(readOnly = true)
    public ReviewResponse execute(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> ReviewExceptions.notFound(ReviewId.of(reviewId)));
        return mapper.toResponse(review);
    }
}
