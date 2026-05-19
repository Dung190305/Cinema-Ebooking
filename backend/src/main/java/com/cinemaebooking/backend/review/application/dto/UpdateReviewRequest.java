package com.cinemaebooking.backend.review.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewRequest {
    private Long userId;
    private Integer rating;
    private String comment;
}
