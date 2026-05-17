package com.cinemaebooking.backend.common.exception.domain;

import com.cinemaebooking.backend.common.exception.BaseException;
import com.cinemaebooking.backend.common.exception.ErrorCode;
import com.cinemaebooking.backend.review.domain.valueobject.ReviewId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReviewExceptions {

    public static BaseException notFound(ReviewId id) {
        return new BaseException(ErrorCode.REVIEW_NOT_FOUND,
                "Không tìm thấy đánh giá: " + id.getValue());
    }

    public static BaseException notOwnedByUser(ReviewId reviewId, Long userId) {
        return new BaseException(ErrorCode.REVIEW_NOT_OWNED_BY_USER,
                String.format("Đánh giá %s không thuộc về người dùng %s", reviewId.getValue(), userId));
    }

    public static BaseException alreadyExists(Long userId, Long movieId) {
        return new BaseException(ErrorCode.REVIEW_ALREADY_EXISTS,
                String.format("Người dùng %d đã đánh giá phim %d rồi", userId, movieId));
    }

    public static BaseException notEligibleToReview(Long bookingId) {
        return new BaseException(ErrorCode.REVIEW_NOT_ELIGIBLE,
                String.format("Booking %d chưa được check-in hoặc chưa thanh toán", bookingId));
    }

    public static BaseException cannotEdit(ReviewId reviewId) {
        return new BaseException(ErrorCode.REVIEW_CANNOT_EDIT,
                "Đánh giá " + reviewId.getValue() + " không thể chỉnh sửa");
    }

    public static BaseException aiRejected() {
        return new BaseException(ErrorCode.REVIEW_AI_REJECTED,
                "Bình luận chứa nội dung không phù hợp và bị từ chối");
    }
}
