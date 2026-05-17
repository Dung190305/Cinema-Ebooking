package com.cinemaebooking.backend.review.application.port;

public interface AIServicePort {

    /**
     * Phân tích comment và trả về sentiment (POSITIVE, NEUTRAL, NEGATIVE).
     * AI model cũng kiểm tra comment có hợp lệ hay không.
     *
     * @param comment nội dung comment cần phân tích
     * @return AiAnalysisResult chứa isValid và sentiment
     */
    AiAnalysisResult analyze(String comment);
}
