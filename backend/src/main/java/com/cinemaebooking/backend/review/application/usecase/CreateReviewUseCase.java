package com.cinemaebooking.backend.review.application.usecase;

import com.cinemaebooking.backend.review.application.dto.CreateReviewRequest;
import com.cinemaebooking.backend.review.application.dto.ReviewResponse;
import com.cinemaebooking.backend.review.application.mapper.ReviewResponseMapper;
import com.cinemaebooking.backend.review.application.port.AIServicePort;
import com.cinemaebooking.backend.review.application.port.ReviewRepository;
import com.cinemaebooking.backend.review.application.validator.ReviewCommandValidator;
import com.cinemaebooking.backend.review.domain.enums.ReviewDecision;
import com.cinemaebooking.backend.review.domain.enums.ReviewStatus;
import com.cinemaebooking.backend.review.domain.model.Review;
import com.cinemaebooking.backend.common.exception.domain.ReviewExceptions;
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

    @Transactional
    public ReviewResponse execute(CreateReviewRequest request) {
        validator.validateCreateRequest(request);

        // Tạo Review với status HIDDEN (chờ AI phân tích)
        Review review = Review.builder()
                .userId(request.getUserId())
                .movieId(request.getMovieId())
                .bookingId(request.getBookingId())
                .rating(request.getRating())
                .comment(request.getComment())
                .status(ReviewStatus.HIDDEN)
                .build();

        // Gọi AI pipeline phân tích comment
        var aiResult = aiService.analyze(request.getComment());

        // Áp dụng AI result vào domain model
        review.applyAiResult(
                aiResult.getSentiment(),
                aiResult.getFinalText(),
                aiResult.isSpoiler(),
                aiResult.getSpoilerConf()
        );

        // Áp dụng decision → status
        review.applyDecision(aiResult.getDecision());

        // Lưu review (dù APPROVED, REJECTED, hay SPOILER_WARNING đều lưu)
        Review saved = reviewRepository.save(review);

        // REJECTED → ném exception nhưng review đã lưu với status = HIDDEN
        if (aiResult.getDecision() == ReviewDecision.REJECTED) {
            throw ReviewExceptions.aiRejected();
        }

        return mapper.toResponse(saved);
    }
}
