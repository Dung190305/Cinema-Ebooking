package com.cinemaebooking.backend.review.infrastructure.adapter;

import com.cinemaebooking.backend.review.application.dto.ApiResponseDto;
import com.cinemaebooking.backend.review.application.port.AIServicePort;
import com.cinemaebooking.backend.review.application.port.AiAnalysisResult;
import com.cinemaebooking.backend.review.domain.enums.ReviewSentiment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class AIServiceAdapter implements AIServicePort {

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;
    private final boolean aiEnabled;

    public AIServiceAdapter(
            RestTemplate restTemplate,
            @Value("${ai.service.url:http://localhost:8081/api/v1/ai/analyze}") String aiServiceUrl,
            @Value("${ai.service.enabled:false}") boolean aiEnabled
    ) {
        this.restTemplate = restTemplate;
        this.aiServiceUrl = aiServiceUrl;
        this.aiEnabled = aiEnabled;
    }

    @Override
    public AiAnalysisResult analyze(String comment) {
        if (!aiEnabled) {
            log.info("AI service disabled, using default result");
            return new AiAnalysisResult(true, ReviewSentiment.NEUTRAL, null);
        }

        try {
            AiRequest request = new AiRequest(comment);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AiRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ApiResponseDto> response = restTemplate.exchange(
                    aiServiceUrl,
                    HttpMethod.POST,
                    entity,
                    ApiResponseDto.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ApiResponseDto body = response.getBody();
                ReviewSentiment sentiment = parseSentiment(body.getSentiment());
                log.info("AI Analysis - Decision: {}, Sentiment: {}", body.getFinalDecision(), sentiment);
                return new AiAnalysisResult(body.isValid(), sentiment, body.getFinalDecision());
            }

            log.warn("AI service returned non-2xx or empty body, using fallback");
        } catch (RestClientException e) {
            log.error("AI service connection failed: {}. Using fallback result.", e.getMessage());
        }

        return new AiAnalysisResult(true, ReviewSentiment.NEUTRAL, null);
    }

    private ReviewSentiment parseSentiment(String value) {
        if (value == null) return ReviewSentiment.NEUTRAL;
        return switch (value.toUpperCase()) {
            case "POSITIVE" -> ReviewSentiment.POSITIVE;
            case "NEGATIVE" -> ReviewSentiment.NEGATIVE;
            default -> ReviewSentiment.NEUTRAL;
        };
    }

    @Getter
    private static class AiRequest {
        private final String text;
        public AiRequest(String text) {
            this.text = text;
        }
    }
}