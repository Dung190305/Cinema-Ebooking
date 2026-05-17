package com.cinemaebooking.backend.review.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

    private Long userId;
    private Long bookingId;
    private Long movieId;
    private Integer rating;
    private String comment;
}
