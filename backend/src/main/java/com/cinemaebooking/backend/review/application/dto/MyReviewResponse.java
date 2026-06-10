package com.cinemaebooking.backend.review.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response trả về cho endpoint GET /reviews/movies/{movieId}/my-review.
 * Nếu chưa review: hasReview = false, review = null
 * Nếu đã review: hasReview = true, review chứa nội dung đã review
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyReviewResponse {

    private boolean hasReview;

    /**
     * Chỉ có giá trị khi hasReview = true.
     * Trả về null khi chưa review.
     */
    private ReviewDetail review;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewDetail {
        private Long reviewId;
        private Long userId;
        private String userName;
        private Long movieId;
        private Long bookingId;
        private Integer rating;
        private String comment;
        private String finalText;
        @JsonProperty("isSpoiler")
        private boolean isSpoiler;
    }
}
