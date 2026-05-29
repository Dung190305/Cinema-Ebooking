package com.cinemaebooking.backend.review.presentation;

import com.cinemaebooking.backend.common.api.response.ApiResponse;
import com.cinemaebooking.backend.review.application.dto.CreateReviewRequest;
import com.cinemaebooking.backend.review.application.dto.MyReviewResponse;
import com.cinemaebooking.backend.review.application.dto.ReviewResponse;
import com.cinemaebooking.backend.review.application.dto.TicketCheckResponse;
import com.cinemaebooking.backend.review.application.dto.UpdateReviewRequest;
import com.cinemaebooking.backend.review.application.usecase.CheckTicketEligibilityUseCase;
import com.cinemaebooking.backend.review.application.usecase.CreateReviewUseCase;
import com.cinemaebooking.backend.review.application.usecase.DeleteReviewUseCase;
import com.cinemaebooking.backend.review.application.usecase.GetMovieReviewsUseCase;
import com.cinemaebooking.backend.review.application.usecase.GetMyReviewUseCase;
import com.cinemaebooking.backend.review.application.usecase.GetReviewDetailUseCase;
import com.cinemaebooking.backend.review.application.usecase.GetUserReviewsUseCase;
import com.cinemaebooking.backend.review.application.usecase.UpdateReviewUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final CreateReviewUseCase createReviewUseCase;
    private final UpdateReviewUseCase updateReviewUseCase;
    private final DeleteReviewUseCase deleteReviewUseCase;
    private final GetReviewDetailUseCase getReviewDetailUseCase;
    private final GetMovieReviewsUseCase getMovieReviewsUseCase;
    private final GetUserReviewsUseCase getUserReviewsUseCase;
    private final GetMyReviewUseCase getMyReviewUseCase;
    private final CheckTicketEligibilityUseCase checkTicketEligibilityUseCase;

    // ================== LIST (DANH SÁCH REVIEW THEO PHIM) ==================
    @GetMapping("/movies/{movieId}")
    public ApiResponse<Page<ReviewResponse>> getMovieReviews(
            @PathVariable Long movieId,
            Pageable pageable,
            HttpServletRequest request) {
        Page<ReviewResponse> result = getMovieReviewsUseCase.execute(movieId, pageable);
        return ApiResponse.success(result, request.getHeader("X-Trace-Id"), request.getRequestURI());
    }

    // ================== LIST (DANH SÁCH REVIEW THEO USER) ==================
    @GetMapping("/users/{userId}")
    public ApiResponse<Page<ReviewResponse>> getUserReviews(
            @PathVariable Long userId,
            Pageable pageable,
            HttpServletRequest request) {
        Page<ReviewResponse> result = getUserReviewsUseCase.execute(userId, pageable);
        return ApiResponse.success(result, request.getHeader("X-Trace-Id"), request.getRequestURI());
    }

    // ================== DETAIL (CHI TIẾT REVIEW) ==================
    @GetMapping("/{id}")
    public ApiResponse<ReviewResponse> getReviewDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        ReviewResponse result = getReviewDetailUseCase.execute(id);
        return ApiResponse.success(result, request.getHeader("X-Trace-Id"), request.getRequestURI());
    }

    // ================== CREATE (TẠO REVIEW) ==================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponse> createReview(
            @RequestBody CreateReviewRequest createRequest,
            HttpServletRequest request) {
        ReviewResponse result = createReviewUseCase.execute(createRequest);
        return ApiResponse.success(result, request.getHeader("X-Trace-Id"), request.getRequestURI());
    }

    // ================== UPDATE (CẬP NHẬT REVIEW) ==================
    @PutMapping("/{id}")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable Long id,
            @RequestBody UpdateReviewRequest updateRequest,
            HttpServletRequest request) {
        ReviewResponse result = updateReviewUseCase.execute(id, updateRequest);
        return ApiResponse.success(result, request.getHeader("X-Trace-Id"), request.getRequestURI());
    }

    // ================== DELETE (XÓA REVIEW) ==================
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @PathVariable Long id,
            @RequestParam Long userId) {
        deleteReviewUseCase.execute(id, userId);
    }

    // ================== MY REVIEW (KIỂM TRA REVIEW CỦA USER HIỆN TẠI) ==================
    @GetMapping("/movies/{movieId}/my-review")
    public ApiResponse<MyReviewResponse> getMyReview(
            @PathVariable Long movieId,
            @RequestParam Long userId,
            HttpServletRequest request) {
        MyReviewResponse result = getMyReviewUseCase.execute(userId, movieId);
        return ApiResponse.success(result, request.getHeader("X-Trace-Id"), request.getRequestURI());
    }

    // ================== CHECK TICKET (KIỂM TRA VÉ ĐÃ CHECK-IN) ==================
    @GetMapping("/movies/{movieId}/check-ticket")
    public ApiResponse<TicketCheckResponse> checkTicketEligibility(
            @PathVariable Long movieId,
            @RequestParam Long userId,
            HttpServletRequest request) {
        TicketCheckResponse result = checkTicketEligibilityUseCase.execute(userId, movieId);
        return ApiResponse.success(result, request.getHeader("X-Trace-Id"), request.getRequestURI());
    }
}
