package com.cinemaebooking.backend.review.application.usecase;

import com.cinemaebooking.backend.review.application.dto.MyReviewResponse;
import com.cinemaebooking.backend.review.application.port.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case kiểm tra và lấy review của user hiện tại cho 1 phim.
 * Dùng cho GET /reviews/movies/{movieId}/my-review
 */
@Service
@RequiredArgsConstructor
public class GetMyReviewUseCase {

    private final ReviewRepository reviewRepository;

    /**
     * Lấy review của user cho phim movieId.
     * Nếu chưa review: trả hasReview = false, review = null
     * Nếu đã review: trả hasReview = true, review chứa nội dung
     */
    @Transactional(readOnly = true)
    public MyReviewResponse execute(Long userId, Long movieId) {
        return reviewRepository.findMyReviewByUserIdAndMovieId(userId, movieId)
                .orElse(MyReviewResponse.builder()
                        .hasReview(false)
                        .review(null)
                        .build());
    }
}
