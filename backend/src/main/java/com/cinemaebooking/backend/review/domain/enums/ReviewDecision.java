package com.cinemaebooking.backend.review.domain.enums;

/**
 * ReviewDecision - Kết quả quyết định cuối cùng từ AI pipeline 3 tầng.
 *
 * <p>Phân loại comment thành 3 nhóm:
 * <ul>
 *   <li>APPROVED: Comment hợp lệ, hiển thị bình thường</li>
 *   <li>REJECTED: Comment vi phạm (toxic/teensex), bị ẩn hoàn toàn</li>
 *   <li>SPOILER_WARNING: Comment hợp lệ nhưng có nội dung spoiler, cần cảnh báo</li>
 * </ul>
 *
 * @author ducthinhn
 * @since 2026
 */
public enum ReviewDecision {
    APPROVED,
    REJECTED,
    SPOILER_WARNING
}
