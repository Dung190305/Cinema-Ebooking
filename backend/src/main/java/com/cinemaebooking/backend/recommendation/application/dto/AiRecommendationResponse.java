package com.cinemaebooking.backend.recommendation.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AiRecommendationResponse {

    @JsonProperty("user_id")
    private Long userId;

    private String source;

    private Boolean cached;

    private List<AiRecommendationItem> recommendations = new ArrayList<>();
}