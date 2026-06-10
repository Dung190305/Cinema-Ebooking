package com.cinemaebooking.backend.review.application.usecase;

import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.common.exception.domain.ReviewExceptions;
import com.cinemaebooking.backend.common.exception.domain.TicketExceptions;
import com.cinemaebooking.backend.review.application.dto.CreateReviewRequest;
import com.cinemaebooking.backend.review.application.dto.ReviewResponse;
import com.cinemaebooking.backend.review.application.mapper.ReviewResponseMapper;
import com.cinemaebooking.backend.review.application.port.AIServicePort;
import com.cinemaebooking.backend.review.application.port.ReviewRepository;
import com.cinemaebooking.backend.review.application.validator.ReviewCommandValidator;
import com.cinemaebooking.backend.review.domain.enums.ReviewDecision;
import com.cinemaebooking.backend.review.domain.enums.ReviewStatus;
import com.cinemaebooking.backend.review.domain.model.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateReviewUseCase {

    private final ReviewRepository reviewRepository;
    private final ReviewCommandValidator validator;
    private final ReviewResponseMapper mapper;
    private final AIServicePort aiService;
    private final BookingJpaRepository bookingJpaRepository;
    @Transactional
    public ReviewResponse execute(CreateReviewRequest request) {
        validator.validateCreateRequest(request);

        BookingJpaEntity booking = bookingJpaRepository
                .findByBookingCodeAndDeletedFalse(request.getBookingCode())
                .orElseThrow(() -> TicketExceptions.notFound(request.getBookingCode()));

        // Tạo Review với status HIDDEN (chờ AI phân tích)
        Review review = Review.builder()
                .userId(request.getUserId())
                .movieId(request.getMovieId())
                .bookingId(booking.getId())
                .rating(request.getRating())
                .comment(request.getComment())
                .status(ReviewStatus.HIDDEN)
                .build();

        // Gọi AI pipeline phân tích comment
        var aiResult = aiService.analyze(request.getComment());

        // Spoiler KHÔNG phải vi phạm — nếu AI trả SPOILER_WARNING thì vẫn duyệt
        ReviewDecision finalDecision = aiResult.getDecision();
        boolean isSpoiler = aiResult.isSpoiler();

        if (finalDecision == ReviewDecision.SPOILER_WARNING) {
            // Đánh dấu spoiler nhưng vẫn duyệt bình luận
            finalDecision = ReviewDecision.APPROVED;
        }

        // Áp dụng AI result vào domain model
        review.applyAiResult(
                aiResult.getSentiment(),
                aiResult.getFinalText(),
                isSpoiler,
                isSpoiler ? aiResult.getSpoilerConf() : 0.0
        );

        // Áp dụng decision cuối cùng → status
        review.applyDecision(finalDecision);

        // Lưu review
        Review saved = reviewRepository.save(review);

        // REJECTED → ném exception (nội dung thực sự vi phạm)
        if (finalDecision == ReviewDecision.REJECTED) {
            throw ReviewExceptions.aiRejected();
        }

        return mapper.toResponse(saved);
    }
}
