package com.cinemaebooking.backend.review.application.port;

public interface AIServicePort {
    AiAnalysisResult analyze(String comment);
}
