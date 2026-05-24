package com.cinemaebooking.backend.movie.application.mapper;

import com.cinemaebooking.backend.movie.application.dto.movie.MovieResponse;
import com.cinemaebooking.backend.movie.domain.model.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MovieResponseMapper {

    private final GenreResponseMapper genreResponseMapper;

    public MovieResponse toResponse(Movie movie) {
        if (movie == null) return null;

        return new MovieResponse(
                movie.getId() != null ? movie.getId().getValue() : null,
                movie.getTitle(),
                movie.getDescription(),
                movie.getDuration(),
                movie.getAgeRating(),
                movie.getReleaseDate(),
                movie.getShowingEndDate(),
                movie.getStatus(),
                movie.getPosterUrl(),
                movie.getBannerUrl(),
                movie.getTrailerUrl(),
                movie.getDirector(),
                movie.getActors(),
                movie.getGenres().stream()
                        .map(genreResponseMapper::toResponse)
                        .collect(Collectors.toSet()),
                movie.getRating(),
                movie.getRatingCount()
        );
    }
}