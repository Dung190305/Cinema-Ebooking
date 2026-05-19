package com.cinemaebooking.backend.movie.domain.model;

import com.cinemaebooking.backend.common.domain.BaseEntity;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import com.cinemaebooking.backend.movie.domain.enums.AgeRating;
import com.cinemaebooking.backend.movie.domain.enums.MovieStatus;
import com.cinemaebooking.backend.movie.domain.valueobject.MovieId;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@SuperBuilder
public class Movie extends BaseEntity<MovieId> {

    private String title;
    private String description;
    private Integer duration;
    private AgeRating ageRating;
    private LocalDate releaseDate;
    private LocalDate showingEndDate;
    private String posterUrl;
    private String bannerUrl;
    private String director;
    private String actors;
    private Set<Genre> genres;
    private Double rating;
    private Integer ratingCount;

    // business methods
    public MovieStatus getStatus() {
        return MovieStatus.from(releaseDate, showingEndDate);
    }

    // ✅ Không còn changeStatus() — thay bằng business intent rõ ràng hơn
    public void markAsEnded() {
        if (this.showingEndDate == null || this.showingEndDate.isAfter(LocalDate.now())) {
            this.showingEndDate = LocalDate.now();
        }
    }

    public void extendShowing(LocalDate newEndDate) {
        if (newEndDate == null || !newEndDate.isAfter(LocalDate.now())) {
            throw CommonExceptions.invalidInput("New end date must be in the future");
        }
        this.showingEndDate = newEndDate;
    }

    public void update(String title, String description, Integer duration,
                       AgeRating ageRating, LocalDate releaseDate, LocalDate showingEndDate,
                       String posterUrl, String bannerUrl, String director, String actors,
                       Set<Genre> genres) {
        validateTitle(title);
        validateDuration(duration);
        validateReleaseDate(releaseDate);
        validateShowingEndDate(releaseDate, showingEndDate);

        this.title = title;
        this.description = description;
        this.duration = duration;
        this.ageRating = ageRating;
        this.releaseDate = releaseDate;
        this.showingEndDate = showingEndDate;
        this.posterUrl = posterUrl;
        this.bannerUrl = bannerUrl;
        this.director = director;
        this.actors = actors;
        this.genres = genres != null ? new HashSet<>(genres) : new HashSet<>();
    }

    // private validators
    private void validateShowingEndDate(LocalDate releaseDate, LocalDate showingEndDate) {
        if (showingEndDate != null && !showingEndDate.isAfter(releaseDate)) {
            throw CommonExceptions.invalidInput("Showing end date must be after release date");
        }
    }


    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw CommonExceptions.invalidInput("Movie title must not be empty");
        }
    }

    private void validateDuration(Integer duration) {
        if (duration == null || duration <= 0) {
            throw CommonExceptions.invalidInput("Duration must be positive");
        }
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate == null) {
            throw CommonExceptions.invalidInput("Release date must not be null");
        }
    }

    private void validateStatus(MovieStatus status) {
        if (status == null) {
            throw CommonExceptions.invalidInput("Movie status must not be null");
        }
    }
}