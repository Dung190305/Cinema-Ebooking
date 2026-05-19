package com.cinemaebooking.backend.review.application.port;

import com.cinemaebooking.backend.review.domain.enums.ReviewSentiment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiAnalysisResult {
    private final boolean isValid;
    private final ReviewSentiment sentiment;
    private final String finalDecision;
}
