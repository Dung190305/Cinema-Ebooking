package com.cinemaebooking.backend.recommendation.presentation;

import com.cinemaebooking.backend.common.security.CustomUserPrincipal;
import com.cinemaebooking.backend.recommendation.application.dto.RecommendationMovieResponse;
import com.cinemaebooking.backend.recommendation.application.usecase.GetUserRecommendationsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final GetUserRecommendationsUseCase getUserRecommendationsUseCase;

    @GetMapping("/me")
    public List<RecommendationMovieResponse> getMyRecommendations(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return getUserRecommendationsUseCase.execute(principal.getUserId(), limit);
    }
}