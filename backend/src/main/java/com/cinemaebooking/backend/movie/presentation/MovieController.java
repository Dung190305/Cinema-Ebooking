package com.cinemaebooking.backend.movie.presentation;

import com.cinemaebooking.backend.movie.application.dto.movie.CreateMovieRequest;
import com.cinemaebooking.backend.movie.application.dto.movie.MovieResponse;
import com.cinemaebooking.backend.movie.application.dto.movie.UpdateMovieRequest;
import com.cinemaebooking.backend.movie.application.usecase.movie.*;
import com.cinemaebooking.backend.movie.domain.enums.AgeRating;
import com.cinemaebooking.backend.movie.domain.enums.MovieStatus;
import com.cinemaebooking.backend.movie.domain.valueobject.MovieId;
import com.cinemaebooking.backend.common.exception.domain.CommonExceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final CreateMovieUseCase createMovieUseCase;
    private final UpdateMovieUseCase updateMovieUseCase;
    private final DeleteMovieUseCase deleteMovieUseCase;
    private final GetMovieDetailUseCase getMovieDetailUseCase;
    private final GetMovieListUseCase getMovieListUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse createMovie(@RequestBody CreateMovieRequest request) {
        return createMovieUseCase.execute(request);
    }

    @PutMapping("/{id}")
    public MovieResponse updateMovie(@PathVariable Long id, @RequestBody UpdateMovieRequest request) {
        return updateMovieUseCase.execute(toMovieId(id), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovie(@PathVariable Long id) {
        deleteMovieUseCase.execute(toMovieId(id));
    }

    @GetMapping("/{id}")
    public MovieResponse getMovieDetail(@PathVariable Long id) {
        return getMovieDetailUseCase.execute(toMovieId(id));
    }

    @GetMapping
    public Page<MovieResponse> getMovieList(
            @PageableDefault(size = 8, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) MovieStatus status,
            @RequestParam(required = false) AgeRating ageRating
    ) {
        return getMovieListUseCase.execute(pageable, status, ageRating);
    }

    private MovieId toMovieId(Long id) {
        if (id == null) throw CommonExceptions.invalidInput("Movie id must not be null");
        return MovieId.of(id);
    }
}