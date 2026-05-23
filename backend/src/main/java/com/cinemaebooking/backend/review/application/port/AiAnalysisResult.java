package com.cinemaebooking.backend.review.application.port;

import com.cinemaebooking.backend.review.domain.enums.ReviewDecision;
import com.cinemaebooking.backend.review.domain.enums.ReviewSentiment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AiAnalysisResult {

    /** Comment có hợp lệ để hiển thị hay không */
    private final boolean isValid;

    /** Cảm xúc của comment */
    private final ReviewSentiment sentiment;

    /** Decision cuối cùng: APPROVED / REJECTED / SPOILER_WARNING */
    private final ReviewDecision decision;

    /** Câu đã chuẩn hóa + censor từ AI (hiển thị cho user) */
    private final String finalText;

    /** Có phải spoiler hay không */
    private final boolean isSpoiler;

    /** Độ tin cậy spoiler (0.0 - 1.0) */
    private final double spoilerConf;

    /** Các từ bị censor trong comment */
    private final java.util.List<String> censoredWords;

    /** Số từ bị censor */
    private final int profanityCount;

    /** Tỷ lệ từ bị censor (0.0 - 1.0) */
    private final double profanityRatio;

    /** Thời gian xử lý (ms) */
    private final double processTimeMs;
}
