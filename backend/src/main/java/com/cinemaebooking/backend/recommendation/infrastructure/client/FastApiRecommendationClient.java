package com.cinemaebooking.backend.recommendation.infrastructure.client;

import com.cinemaebooking.backend.recommendation.application.dto.AiRecommendationResponse;
import com.cinemaebooking.backend.recommendation.application.port.RecommendationAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class FastApiRecommendationClient implements RecommendationAiClient {

    private final RestTemplate recommendationRestTemplate;

    @Value("${ai.recommendation.base-url:http://localhost:8000}")
    private String baseUrl;

    @Override
    public AiRecommendationResponse getRecommendations(Long userId, int limit) {
        String url = baseUrl + "/recommendations/" + userId + "?limit=" + limit;

        AiRecommendationResponse response =
                recommendationRestTemplate.getForObject(url, AiRecommendationResponse.class);

        if (response == null) {
            AiRecommendationResponse empty = new AiRecommendationResponse();
            empty.setUserId(userId);
            empty.setSource("empty");
            empty.setCached(false);
            return empty;
        }

        return response;
    }
}