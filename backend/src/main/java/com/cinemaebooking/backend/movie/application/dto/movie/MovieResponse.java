package com.cinemaebooking.backend.movie.application.dto.movie;

import com.cinemaebooking.backend.movie.application.dto.genre.GenreResponse;
import com.cinemaebooking.backend.movie.domain.enums.AgeRating;
import com.cinemaebooking.backend.movie.domain.enums.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@AllArgsConstructor
public class MovieResponse {
    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private AgeRating ageRating;
    private LocalDate releaseDate;
    private LocalDate showingEndDate;
    private MovieStatus status;
    private String posterUrl;
    private String bannerUrl;
    private String trailerUrl;
    private String director;
    private String actors;
    private Set<GenreResponse> genres;
    private Double rating;
    private Integer ratingCount;
}