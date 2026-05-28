package com.cinemaebooking.backend.review.infrastructure.persistence.entity;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.infrastructure.persistence.entity.BaseJpaEntity;
import com.cinemaebooking.backend.review.domain.enums.ReviewDecision;
import com.cinemaebooking.backend.review.domain.enums.ReviewSentiment;
import com.cinemaebooking.backend.review.domain.enums.ReviewStatus;
import com.cinemaebooking.backend.user.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * ReviewJpaEntity - Persistence model cho bảng reviews.
 *
 * <p>Bổ sung 3 cột mới từ AI pipeline:
 * <ul>
 *   <li>is_spoiler  : đánh dấu comment có spoiler</li>
 *   <li>final_text  : câu đã chuẩn hóa + censor (hiển thị cho user)</li>
 *   <li>spoiler_conf: độ tin cậy spoiler (0.0 - 1.0)</li>
 *   <li>decision    : kết quả quyết định từ AI</li>
 * </ul>
 *
 * @author Hieu Nguyen
 * @since 2026
 */
@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reviews_user_id_movie_id_deleted",
                        columnNames = {"user_id", "movie_id", "deleted"}
                ),
                @UniqueConstraint(
                        name = "uk_reviews_booking_id_deleted",
                        columnNames = {"booking_id", "deleted"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ReviewJpaEntity extends BaseJpaEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @NotNull
    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingJpaEntity booking;

    @NotNull
    @Min(1)
    @Max(10)
    @Column(nullable = false)
    private Integer rating;

    /** Nội dung gốc user nhập (lưu để audit) */
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    /** Câu đã chuẩn hóa + censor từ AI (hiển thị cho user) */
    @Column(name = "final_text", columnDefinition = "TEXT")
    private String finalText;

    /** Cảm xúc phân tích từ AI */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewSentiment sentiment;

    /** Decision cuối cùng từ AI: APPROVED / REJECTED / SPOILER_WARNING */
    @Enumerated(EnumType.STRING)
    @Column(name = "decision", length = 20)
    private ReviewDecision decision;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewStatus status;

    /** Đánh dấu comment có chứa spoiler */
    @NotNull
    @Column(name = "is_spoiler", nullable = false)
    @Builder.Default
    private boolean isSpoiler = false;

    /** Độ tin cậy spoiler detection (0.0 - 1.0) */
    @NotNull
    @Column(name = "spoiler_conf", nullable = false)
    @Builder.Default
    private double spoilerConf = 0.0;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Override
    protected void beforeSoftDelete() {
        // Review là dữ liệu quan trọng, không cần mark gì đặc biệt khi xóa
    }
}
