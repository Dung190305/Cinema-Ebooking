package com.cinemaebooking.backend.review.application.validator;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.ReviewExceptions;
import com.cinemaebooking.backend.review.application.dto.CreateReviewRequest;
import com.cinemaebooking.backend.review.application.dto.UpdateReviewRequest;
import com.cinemaebooking.backend.review.application.port.ReviewEligibilityPort;
import com.cinemaebooking.backend.review.application.port.ReviewRepository;
import com.cinemaebooking.backend.review.domain.valueobject.ReviewId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewCommandValidator {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 10;
    private static final int MIN_COMMENT_LENGTH = 2;
    private static final int MAX_COMMENT_LENGTH = 2000;

    private final ReviewRepository reviewRepository;
    private final ReviewEligibilityPort eligibilityPort;

    public void validateCreateRequest(CreateReviewRequest request) {
        if (request == null) {
            throw CommonExceptions.invalidInput("Create review request must not be null");
        }

        if (request.getUserId() == null) {
            throw CommonExceptions.invalidInput("userId is required");
        }
        if (request.getBookingId() == null) {
            throw CommonExceptions.invalidInput("bookingId is required");
        }
        if (request.getMovieId() == null) {
            throw CommonExceptions.invalidInput("movieId is required");
        }

        validateRating(request.getRating());
        validateComment(request.getComment());

        if (reviewRepository.existsByUserIdAndMovieId(request.getUserId(), request.getMovieId())) {
            throw ReviewExceptions.alreadyExists(request.getUserId(), request.getMovieId());
        }

        if (!eligibilityPort.isEligibleToReview(request.getUserId(), request.getBookingId())) {
            throw ReviewExceptions.notEligibleToReview(request.getBookingId());
        }
    }

    public void validateUpdateRequest(UpdateReviewRequest request, Long reviewId) {
        if (request == null) {
            throw CommonExceptions.invalidInput("Update review request must not be null");
        }

        if (request.getUserId() == null) {
            throw CommonExceptions.invalidInput("userId is required");
        }

        validateRating(request.getRating());
        validateComment(request.getComment());

        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> ReviewExceptions.notFound(ReviewId.of(reviewId)));

        if (!review.getUserId().equals(request.getUserId())) {
            throw ReviewExceptions.notOwnedByUser(ReviewId.of(reviewId), request.getUserId());
        }
    }

    private void validateRating(Integer rating) {
        if (rating == null) {
            throw CommonExceptions.invalidInput("rating is required");
        }
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw CommonExceptions.invalidInput(
                    String.format("Rating must be between %d and %d", MIN_RATING, MAX_RATING));
        }
    }

    private void validateComment(String comment) {
        if (comment == null || comment.isBlank()) {
            throw CommonExceptions.invalidInput("comment must not be blank");
        }
        if (comment.length() < MIN_COMMENT_LENGTH) {
            throw CommonExceptions.invalidInput(
                    String.format("Comment must be at least %d characters", MIN_COMMENT_LENGTH));
        }
        if (comment.length() > MAX_COMMENT_LENGTH) {
            throw CommonExceptions.invalidInput(
                    String.format("Comment must not exceed %d characters", MAX_COMMENT_LENGTH));
        }
    }
}
