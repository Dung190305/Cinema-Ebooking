package com.cinemaebooking.backend.recommendation.application.port;

import com.cinemaebooking.backend.recommendation.application.dto.AiRecommendationResponse;

public interface RecommendationAiClient {

    AiRecommendationResponse getRecommendations(Long userId, int limit);
}