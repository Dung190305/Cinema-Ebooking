package com.cinemaebooking.backend.review.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.review.domain.enums.ReviewSentiment;
import com.cinemaebooking.backend.review.domain.enums.ReviewStatus;
import com.cinemaebooking.backend.review.domain.valueobject.ReviewId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
public class Review extends BaseEntity<ReviewId> {

    private final Long userId;
    private final Long movieId;
    private final Long bookingId;

    private Integer rating;
    private String comment;
    private ReviewSentiment sentiment;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;

    public void edit(Integer newRating, String newComment) {
        if (this.status != ReviewStatus.ACTIVE) {
            throw CommonExceptions.invalidInput("Chỉ review đang hiển thị mới được chỉnh sửa.");
        }
        this.rating = newRating;
        this.comment = newComment;
        this.editedAt = LocalDateTime.now();
    }

    public void applyAiResult(boolean isValid, ReviewSentiment aiSentiment) {
        this.sentiment = aiSentiment;
        this.status = isValid ? ReviewStatus.ACTIVE : ReviewStatus.HIDDEN;
    }
}
