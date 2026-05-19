package com.cinemaebooking.backend.movie.application.port;

import com.cinemaebooking.backend.movie.domain.enums.AgeRating;
import com.cinemaebooking.backend.movie.domain.enums.MovieStatus;
import com.cinemaebooking.backend.movie.domain.model.Movie;
import com.cinemaebooking.backend.movie.domain.valueobject.MovieId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MovieRepository {
    Movie create(Movie movie);
    Movie update(Movie movie);
    Optional<Movie> findById(MovieId id);
    Page<Movie> findAll(Pageable pageable);
    Page<Movie> findByFilters(MovieStatus status, AgeRating ageRating, Pageable pageable);
    void deleteById(MovieId id);
    boolean existsById(MovieId id);
    boolean existsByTitle(String title);
    boolean existsByTitleAndIdNot(String title, MovieId id);
}