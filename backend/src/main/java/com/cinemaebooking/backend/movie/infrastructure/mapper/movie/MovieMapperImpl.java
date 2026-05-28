
package com.cinemaebooking.backend.movie.infrastructure.mapper.movie;

import com.cinemaebooking.backend.movie.domain.model.Movie;
import com.cinemaebooking.backend.movie.domain.valueobject.MovieId;
import com.cinemaebooking.backend.movie.infrastructure.mapper.genre.GenreMapper;
import com.cinemaebooking.backend.movie.infrastructure.persistence.entity.MovieJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MovieMapperImpl implements MovieMapper {

    private final GenreMapper genreMapper;

    @Override
    public MovieJpaEntity toEntity(Movie domain) {
        if (domain == null) return null;

        return MovieJpaEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .title(domain.getTitle())
                .description(domain.getDescription())
                .duration(domain.getDuration())
                .ageRating(domain.getAgeRating())
                .releaseDate(domain.getReleaseDate())
                .showingEndDate(domain.getShowingEndDate())
                .posterUrl(domain.getPosterUrl())
                .bannerUrl(domain.getBannerUrl())
                .trailerUrl(domain.getTrailerUrl())
                .director(domain.getDirector())
                .actors(domain.getActors())
                .genres(
                        domain.getGenres() == null
                                ? new HashSet<>()
                                : domain.getGenres().stream()
                                .map(genreMapper::toEntity)
                                .collect(Collectors.toSet())
                )
                .rating(domain.getRating())
                .ratingCount(domain.getRatingCount())
                .build();
    }

    @Override
    public Movie toDomain(MovieJpaEntity entity) {
        if (entity == null) return null;

        return Movie.builder()
                .id(MovieId.ofNullable(entity.getId()))
                .title(entity.getTitle())
                .description(entity.getDescription())
                .duration(entity.getDuration())
                .ageRating(entity.getAgeRating())
                .releaseDate(entity.getReleaseDate())
                .showingEndDate(entity.getShowingEndDate())
                .posterUrl(entity.getPosterUrl())
                .bannerUrl(entity.getBannerUrl())
                .trailerUrl(entity.getTrailerUrl())
                .director(entity.getDirector())
                .actors(entity.getActors())
                .genres(entity.getGenres() == null
                        ? new HashSet<>()
                        : entity.getGenres().stream()
                        .map(genreMapper::toDomain)
                        .collect(Collectors.toSet()))
                .rating(entity.getRating())
                .ratingCount(entity.getRatingCount())
                .build();
    }

    @Override
    public void updateEntity(MovieJpaEntity entity, Movie domain) {
        if (entity == null || domain == null) return;

        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setDuration(domain.getDuration());
        entity.setAgeRating(domain.getAgeRating());
        entity.setReleaseDate(domain.getReleaseDate());
        entity.setShowingEndDate(domain.getShowingEndDate());
        entity.setPosterUrl(domain.getPosterUrl());
        entity.setBannerUrl(domain.getBannerUrl());
        entity.setTrailerUrl(domain.getTrailerUrl());
        entity.setDirector(domain.getDirector());
        entity.setActors(domain.getActors());
        entity.getGenres().clear();

        if (domain.getGenres() != null) {
            entity.getGenres().addAll(
                    domain.getGenres().stream()
                            .map(genreMapper::toEntity)
                            .collect(Collectors.toSet())
            );
        }
    }
}