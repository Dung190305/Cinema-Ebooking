package com.cinemaebooking.backend.review.infrastructure.adapter;

import com.cinemaebooking.backend.review.application.dto.ApiResponseDto;
import com.cinemaebooking.backend.review.application.port.AIServicePort;
import com.cinemaebooking.backend.review.application.port.AiAnalysisResult;
import com.cinemaebooking.backend.review.domain.enums.ReviewDecision;
import com.cinemaebooking.backend.review.domain.enums.ReviewSentiment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * AIServiceAdapter - Gọi FastAPI AI service để phân tích comment.
 *
 * <p>FastAPI trả về đầy đủ thông tin từ pipeline 3 tầng:
 * <ol>
 *   <li>Tầng 1: Hard Filter (toxic/teensex) → cleanedText, censoredWords</li>
 *   <li>Tầng 2: Sentiment Analysis → sentiment, sentimentScore</li>
 *   <li>Tầng 3: Spoiler Detection → isSpoiler, spoilerConf</li>
 * </ol>
 *
 * @author ducthinhn
 * @since 2026
 */
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
            log.info("AI service disabled, using NEUTRAL + APPROVED fallback");
            return AiAnalysisResult.builder()
                    .isValid(true)
                    .sentiment(ReviewSentiment.NEUTRAL)
                    .decision(ReviewDecision.APPROVED)
                    .finalText(comment)
                    .isSpoiler(false)
                    .spoilerConf(0.0)
                    .censoredWords(List.of())
                    .profanityCount(0)
                    .profanityRatio(0.0)
                    .processTimeMs(0.0)
                    .build();
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
                return toAiResult(response.getBody());
            }

            log.warn("AI service returned non-2xx or empty body, using fallback");
        } catch (RestClientException e) {
            log.error("AI service connection failed: {}. Using fallback result.", e.getMessage());
        }

        // Fallback: coi comment là hợp lệ
        return AiAnalysisResult.builder()
                .isValid(true)
                .sentiment(ReviewSentiment.NEUTRAL)
                .decision(ReviewDecision.APPROVED)
                .finalText(comment)
                .isSpoiler(false)
                .spoilerConf(0.0)
                .censoredWords(List.of())
                .profanityCount(0)
                .profanityRatio(0.0)
                .processTimeMs(0.0)
                .build();
    }

    private AiAnalysisResult toAiResult(ApiResponseDto dto) {
        ReviewSentiment sentiment = parseSentiment(dto.getLabel());
        ReviewDecision decision = parseDecision(dto.getFinalDecision());

        log.info("AI Analysis - Decision: {}, Sentiment: {}, Spoiler: {}, isValid: {}",
                decision, sentiment, dto.isSpoiler(), dto.isValid());

        return AiAnalysisResult.builder()
                .isValid(dto.isValid())
                .sentiment(sentiment)
                .decision(decision)
                .finalText(dto.getFinalOutput() != null ? dto.getFinalOutput() : dto.getCleanedText())
                .isSpoiler(dto.isSpoiler())
                .spoilerConf(dto.getSpoilerConf())
                .censoredWords(dto.getCensoredWords() != null ? dto.getCensoredWords() : List.of())
                .profanityCount(dto.getProfanityCount())
                .profanityRatio(dto.getProfanityRatio())
                .processTimeMs(dto.getProcessTime())
                .build();
    }

    private ReviewSentiment parseSentiment(String label) {
        if (label == null) return ReviewSentiment.NEUTRAL;
        return switch (label.toUpperCase()) {
            case "POSITIVE" -> ReviewSentiment.POSITIVE;
            case "NEGATIVE" -> ReviewSentiment.NEGATIVE;
            default -> ReviewSentiment.NEUTRAL;  // Spoiler → coi là neutral cho sentiment
        };
    }

    private ReviewDecision parseDecision(String value) {
        if (value == null) return ReviewDecision.APPROVED;
        return switch (value.toUpperCase()) {
            case "REJECTED" -> ReviewDecision.REJECTED;
            case "SPOILER_WARNING" -> ReviewDecision.SPOILER_WARNING;
            default -> ReviewDecision.APPROVED;
        };
    }
    @Getter
    private static class AiRequest {
        private final String text;
        public AiRequest(String text) { this.text = text; }
    }
}
