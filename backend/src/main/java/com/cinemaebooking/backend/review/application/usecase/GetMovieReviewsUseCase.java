package com.cinemaebooking.backend.review.application.usecase;

import com.cinemaebooking.backend.review.application.dto.ReviewResponse;
import com.cinemaebooking.backend.review.application.mapper.ReviewResponseMapper;
import com.cinemaebooking.backend.review.application.port.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetMovieReviewsUseCase {

    private final ReviewRepository reviewRepository;
    private final ReviewResponseMapper mapper;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> execute(Long movieId, Pageable pageable) {
        return reviewRepository.findByMovieId(movieId, pageable)
                .map(mapper::toResponse);
    }
}
