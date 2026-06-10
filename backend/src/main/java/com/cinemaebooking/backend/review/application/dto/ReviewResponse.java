package com.cinemaebooking.backend.review.application.dto;

import com.cinemaebooking.backend.review.domain.enums.ReviewDecision;
import com.cinemaebooking.backend.review.domain.enums.ReviewSentiment;
import com.cinemaebooking.backend.review.domain.enums.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String finalText;
    private ReviewSentiment sentiment;
    private ReviewDecision decision;
    private ReviewStatus status;
    @JsonProperty("isSpoiler")
    private boolean isSpoiler;
    private double spoilerConf;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private boolean edited;
}
