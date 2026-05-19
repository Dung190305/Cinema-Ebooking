/*
package com.cinemaebooking.backend.movie.application.usecase.movie;

import com.cinemaebooking.backend.movie.application.dto.movie.MovieResponse;
import com.cinemaebooking.backend.movie.application.dto.movie.UpdateMovieRequest;
import com.cinemaebooking.backend.movie.application.mapper.MovieResponseMapper;
import com.cinemaebooking.backend.movie.application.port.GenreRepository;
import com.cinemaebooking.backend.movie.application.port.MovieRepository;
import com.cinemaebooking.backend.movie.application.validator.MovieCommandValidator;
import com.cinemaebooking.backend.movie.domain.model.Genre;
import com.cinemaebooking.backend.movie.domain.model.Movie;
import com.cinemaebooking.backend.movie.domain.valueobject.GenreId;
import com.cinemaebooking.backend.movie.domain.valueobject.MovieId;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.MovieExceptions;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateMovieUseCase {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieResponseMapper mapper;
    private final MovieCommandValidator validator;

    public MovieResponse execute(MovieId id, UpdateMovieRequest request) {
        validator.validateUpdateRequest(id, request);

        Movie movie = loadMovie(id);
        Set<Genre> genres = resolveGenres(request.getGenreIds());

        Set<Genre> finalGenres = (genres != null) ? genres : movie.getGenres();

        movie.update(
                request.getTitle(),
                request.getDescription(),
                request.getDuration(),
                request.getAgeRating(),
                request.getReleaseDate(),
                request.getStatus(),
                request.getPosterUrl(),
                request.getBannerUrl(),
                request.getDirector(),
                request.getActors(),
                genres
        );

        Movie saved = persist(movie);
        return mapper.toResponse(saved);
    }

    private Movie loadMovie(MovieId id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> MovieExceptions.notFound(id));
    }

    private Set<Genre> resolveGenres(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) return Set.of();
        Set<GenreId> ids = genreIds.stream()
                .map(GenreId::of)
                .collect(Collectors.toSet());
        return genreRepository.findAllByIds(ids);
    }

    private Movie persist(Movie movie) {
        try {
            return movieRepository.update(movie);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException ex) {
            throw CommonExceptions.concurrencyConflict();
        }
    }
}

 */
package com.cinemaebooking.backend.movie.application.usecase.movie;

import com.cinemaebooking.backend.movie.application.dto.movie.MovieResponse;
import com.cinemaebooking.backend.movie.application.dto.movie.UpdateMovieRequest;
import com.cinemaebooking.backend.movie.application.mapper.MovieResponseMapper;
import com.cinemaebooking.backend.movie.application.port.GenreRepository;
import com.cinemaebooking.backend.movie.application.port.MovieRepository;
import com.cinemaebooking.backend.movie.application.validator.MovieCommandValidator;
import com.cinemaebooking.backend.movie.domain.model.Genre;
import com.cinemaebooking.backend.movie.domain.model.Movie;
import com.cinemaebooking.backend.movie.domain.valueobject.GenreId;
import com.cinemaebooking.backend.movie.domain.valueobject.MovieId;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.common.exception.domain.GenreExceptions;
import com.cinemaebooking.backend.common.exception.domain.MovieExceptions;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateMovieUseCase {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieResponseMapper mapper;
    private final MovieCommandValidator validator;

    public MovieResponse execute(MovieId id, UpdateMovieRequest request) {
        validator.validateUpdateRequest(id, request);

        Movie movie = loadMovie(id);

        // Determine final genres
        Set<Genre> finalGenres;
        if (request.getGenreIds() == null) {
            finalGenres = movie.getGenres(); // keep existing
        } else {
            finalGenres = resolveGenres(request.getGenreIds()); // validate & fetch
        }

        movie.update(
                request.getTitle(),
                request.getDescription(),
                request.getDuration(),
                request.getAgeRating(),
                request.getReleaseDate(),
                request.getShowingEndDate(),
                request.getPosterUrl(),
                request.getBannerUrl(),
                request.getDirector(),
                request.getActors(),
                finalGenres
        );

        Movie saved = persist(movie);
        return mapper.toResponse(saved);
    }

    private Movie loadMovie(MovieId id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> MovieExceptions.notFound(id));
    }

    private Set<Genre> resolveGenres(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            throw CommonExceptions.invalidInput("Movie must have at least one genre");
        }

        Set<GenreId> ids = genreIds.stream()
                .map(GenreId::of)
                .collect(Collectors.toSet());

        Set<Genre> genres = genreRepository.findAllByIds(ids);

        if (genres.size() != ids.size()) {
            Set<Long> foundIds = genres.stream()
                    .map(g -> g.getId().getValue())
                    .collect(Collectors.toSet());
            Set<Long> missingIds = ids.stream()
                    .map(GenreId::getValue)
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());
            throw GenreExceptions.notFound(missingIds);
        }
        return genres;
    }

    private Movie persist(Movie movie) {
        try {
            return movieRepository.update(movie);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException ex) {
            throw CommonExceptions.concurrencyConflict();
        }
    }
}