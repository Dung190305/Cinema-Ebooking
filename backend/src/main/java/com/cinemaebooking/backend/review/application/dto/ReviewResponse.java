package com.cinemaebooking.backend.review.application.dto;

import com.cinemaebooking.backend.review.domain.enums.ReviewSentiment;
import com.cinemaebooking.backend.review.domain.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long reviewId;
    private Long userId;
    private String userName;
    private Long movieId;
    private Long bookingId;
    private Integer rating;
    private String comment;
    private ReviewSentiment sentiment;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private boolean edited;
}
