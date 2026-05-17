package com.cinemaebooking.backend.review.application.usecase;

import com.cinemaebooking.backend.common.exception.domain.ReviewExceptions;
import com.cinemaebooking.backend.review.application.port.ReviewRepository;
import com.cinemaebooking.backend.review.domain.model.Review;
import com.cinemaebooking.backend.review.domain.valueobject.ReviewId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteReviewUseCase {

    private final ReviewRepository reviewRepository;

    @Transactional
    public void execute(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> ReviewExceptions.notFound(ReviewId.of(reviewId)));

        if (!review.getUserId().equals(userId)) {
            throw ReviewExceptions.notOwnedByUser(ReviewId.of(reviewId), userId);
        }

        reviewRepository.delete(review);
    }
}
