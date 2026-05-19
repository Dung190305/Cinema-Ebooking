package com.cinemaebooking.backend.review.application.usecase;

import com.cinemaebooking.backend.review.application.dto.ReviewResponse;
import com.cinemaebooking.backend.review.application.dto.UpdateReviewRequest;
import com.cinemaebooking.backend.review.application.mapper.ReviewResponseMapper;
import com.cinemaebooking.backend.review.application.port.AiAnalysisResult;
import com.cinemaebooking.backend.review.application.port.AIServicePort;
import com.cinemaebooking.backend.review.application.port.ReviewRepository;
import com.cinemaebooking.backend.review.application.validator.ReviewCommandValidator;
import com.cinemaebooking.backend.review.domain.model.Review;
import com.cinemaebooking.backend.review.domain.valueobject.ReviewId;
import com.cinemaebooking.backend.common.exception.domain.ReviewExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateReviewUseCase {

    private final ReviewRepository reviewRepository;
    private final ReviewCommandValidator validator;
    private final ReviewResponseMapper mapper;
    private final AIServicePort aiService;

    @Transactional
    public ReviewResponse execute(Long reviewId, UpdateReviewRequest request) {
        validator.validateUpdateRequest(request, reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> ReviewExceptions.notFound(ReviewId.of(reviewId)));

        review.edit(request.getRating(), request.getComment());

        // Gọi AI phân tích lại comment sau khi chỉnh sửa
        AiAnalysisResult aiResult = aiService.analyze(request.getComment());
        review.applyAiResult(aiResult.isValid(), aiResult.getSentiment());

        Review saved = reviewRepository.save(review);
        return mapper.toResponse(saved);
    }
}
