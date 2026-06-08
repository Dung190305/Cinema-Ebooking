package com.cinemaebooking.backend.recommendation.application.usecase;

import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.movie.application.dto.genre.GenreResponse;
import com.cinemaebooking.backend.movie.domain.enums.MovieStatus;
import com.cinemaebooking.backend.movie.infrastructure.persistence.entity.GenreJpaEntity;
import com.cinemaebooking.backend.movie.infrastructure.persistence.entity.MovieJpaEntity;
import com.cinemaebooking.backend.movie.infrastructure.persistence.repository.MovieJpaRepository;
import com.cinemaebooking.backend.recommendation.application.dto.AiRecommendationItem;
import com.cinemaebooking.backend.recommendation.application.dto.AiRecommendationResponse;
import com.cinemaebooking.backend.recommendation.application.dto.RecommendationMovieResponse;
import com.cinemaebooking.backend.recommendation.application.port.RecommendationAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetUserRecommendationsUseCase {

    private final RecommendationAiClient recommendationAiClient;
    private final MovieJpaRepository movieJpaRepository;

    public List<RecommendationMovieResponse> execute(Long userId, int limit) {
        if (userId == null || userId <= 0) {
            throw CommonExceptions.invalidInput("User id must be positive");
        }

        AiRecommendationResponse aiResponse =
                recommendationAiClient.getRecommendations(userId, limit);

        if (aiResponse.getRecommendations() == null || aiResponse.getRecommendations().isEmpty()) {
            return List.of();
        }

        List<Long> movieIds = aiResponse.getRecommendations()
                .stream()
                .map(AiRecommendationItem::getMovieId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (movieIds.isEmpty()) {
            return List.of();
        }

        List<MovieJpaEntity> movies = movieJpaRepository.findAllByIdInWithGenres(movieIds);

        Map<Long, MovieJpaEntity> movieMap = movies.stream()
                .collect(Collectors.toMap(MovieJpaEntity::getId, Function.identity()));

        Map<Long, AiRecommendationItem> scoreMap = aiResponse.getRecommendations()
                .stream()
                .filter(item -> item.getMovieId() != null)
                .collect(Collectors.toMap(
                        AiRecommendationItem::getMovieId,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<RecommendationMovieResponse> result = new ArrayList<>();

        for (Long movieId : movieIds) {
            MovieJpaEntity movie = movieMap.get(movieId);
            AiRecommendationItem item = scoreMap.get(movieId);

            if (movie == null || item == null) {
                continue;
            }

            result.add(toResponse(movie, item));
        }

        return result;
    }

    private RecommendationMovieResponse toResponse(
            MovieJpaEntity movie,
            AiRecommendationItem item
    ) {
        Set<GenreResponse> genres = movie.getGenres() == null
                ? Set.of()
                : movie.getGenres()
                  .stream()
                  .map(this::toGenreResponse)
                  .collect(Collectors.toSet());

        MovieStatus status = MovieStatus.from(
                movie.getReleaseDate(),
                movie.getShowingEndDate()
        );

        return new RecommendationMovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getDuration(),
                movie.getAgeRating(),
                movie.getReleaseDate(),
                movie.getShowingEndDate(),
                status,
                movie.getPosterUrl(),
                movie.getBannerUrl(),
                movie.getDirector(),
                movie.getActors(),
                genres,
                movie.getRating(),
                movie.getRatingCount(),
                item.getScore(),
                item.getSource()
        );
    }

    private GenreResponse toGenreResponse(GenreJpaEntity genre) {
        return new GenreResponse(
                genre.getId(),
                genre.getName()
        );
    }
}