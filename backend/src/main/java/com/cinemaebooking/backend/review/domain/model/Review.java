package com.cinemaebooking.backend.review.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.review.domain.enums.ReviewDecision;
import com.cinemaebooking.backend.review.domain.enums.ReviewSentiment;
import com.cinemaebooking.backend.review.domain.enums.ReviewStatus;
import com.cinemaebooking.backend.review.domain.valueobject.ReviewId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder(toBuilder = true)
public class Review extends BaseEntity<ReviewId> {

    private Long userId;
    private Long movieId;
    private Long bookingId;

    private Integer rating;
    private String comment;
    private String finalText;
    private ReviewSentiment sentiment;
    private ReviewDecision decision;
    private ReviewStatus status;
    private boolean isSpoiler;
    private double spoilerConf;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;

    // =======================================================================
    // DOMAIN LOGIC
    // =======================================================================

    /**
     * Kiểm tra comment có hiển thị được hay không.
     *
     * @return true nếu status = ACTIVE
     */
    public boolean isVisible() {
        return status == ReviewStatus.ACTIVE;
    }

    /**
     * Kiểm tra comment có cần cảnh báo spoiler hay không.
     * Dùng để FE hiển thị UI spoiler cho user.
     *
     * @return true nếu là spoiler (decision = SPOILER_WARNING)
     */
    public boolean needsSpoilerWarning() {
        return isSpoiler && decision == ReviewDecision.SPOILER_WARNING;
    }

    /**
     * Lấy nội dung hiển thị cho user.
     * Ưu tiên finalText (đã chuẩn hóa) nếu có, fallback về comment gốc.
     *
     * @return nội dung an toàn để hiển thị
     */
    public String getDisplayText() {
        return finalText != null ? finalText : comment;
    }

    /**
     * Chỉnh sửa nội dung review.
     * Sau khi edit, AI sẽ phân tích lại → applyAiResult cần được gọi sau.
     *
     * @param newRating điểm mới (1-10)
     * @param newComment bình luận mới
     */
    public void edit(Integer newRating, String newComment) {
        if (!isVisible()) {
            throw CommonExceptions.invalidInput("Chỉ review đang hiển thị mới được chỉnh sửa.");
        }
        this.rating = newRating;
        this.comment = newComment;
        // Reset AI fields vì comment đã thay đổi, cần re-analyze
        this.finalText = null;
        this.isSpoiler = false;
        this.spoilerConf = 0.0;
        this.editedAt = LocalDateTime.now();
    }

    /**
     * Áp dụng kết quả AI sau khi phân tích comment.
     * Cập nhật sentiment, decision, isSpoiler, finalText và status.
     *
     * @param sentiment cảm xúc (POSITIVE/NEUTRAL/NEGATIVE)
     * @param finalText câu đã chuẩn hóa từ AI
     * @param isSpoiler có phải spoiler hay không
     * @param spoilerConf độ tin cậy spoiler (0.0 - 1.0)
     */
    public void applyAiResult(
            ReviewSentiment sentiment,
            String finalText,
            boolean isSpoiler,
            double spoilerConf
    ) {
        this.sentiment = sentiment;
        this.finalText = finalText;
        this.isSpoiler = isSpoiler;
        this.spoilerConf = spoilerConf;
        this.editedAt = LocalDateTime.now();
    }

    /**
     * Áp dụng decision cuối cùng vào status.
     * APPROVED → ACTIVE, REJECTED → HIDDEN, SPOILER_WARNING → ACTIVE (nhưng isSpoiler=true)
     *
     * @param decision kết quả quyết định từ AI
     */
    public void applyDecision(ReviewDecision decision) {
        this.decision = decision;
        switch (decision) {
            case APPROVED, SPOILER_WARNING -> this.status = ReviewStatus.ACTIVE;
            case REJECTED -> this.status = ReviewStatus.HIDDEN;
        }
    }
}
