package com.cinemaebooking.backend.recommendation.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AiRecommendationItem {

    @JsonProperty("movie_id")
    private Long movieId;

    private Double score;

    private String source;
}