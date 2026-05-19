
package com.cinemaebooking.backend.movie.infrastructure.adapter;

import com.cinemaebooking.backend.movie.application.port.MovieRepository;
import com.cinemaebooking.backend.movie.domain.enums.AgeRating;
import com.cinemaebooking.backend.movie.domain.enums.MovieStatus;
import com.cinemaebooking.backend.movie.domain.model.Movie;
import com.cinemaebooking.backend.movie.domain.valueobject.MovieId;
import com.cinemaebooking.backend.movie.infrastructure.mapper.movie.MovieMapper;
import com.cinemaebooking.backend.movie.infrastructure.persistence.entity.MovieJpaEntity;
import com.cinemaebooking.backend.movie.infrastructure.persistence.repository.MovieJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieRepositoryImpl implements MovieRepository {

    private final MovieJpaRepository jpaRepository;
    private final MovieMapper mapper;

    @Override
    @Transactional
    public Movie create(Movie movie) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(movie)));
    }

    @Override
    @Transactional
    public Movie update(Movie movie) {
        MovieJpaEntity entity = jpaRepository.findByIdOrThrow(movie.getId().getValue());
        mapper.updateEntity(entity, movie);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Movie> findById(MovieId id) {
        return jpaRepository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public Page<Movie> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Movie> findByFilters(MovieStatus status, AgeRating ageRating, Pageable pageable) {
        return jpaRepository.findByFilters(status, ageRating, pageable)
                .map(mapper::toDomain);
    }
/*
    @Override
    public void deleteById(MovieId id) {
        MovieJpaEntity entity = jpaRepository.findByIdOrThrow(id.getValue());
        jpaRepository.delete(entity);
    }

 */
    @Override
    @Transactional
    public void deleteById(MovieId id) {
        jpaRepository.softDeleteById(id.getValue());
    }

    @Override
    public boolean existsById(MovieId id) {
        return jpaRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsByTitle(String title) {
        return jpaRepository.existsByTitle(title);
    }

    @Override
    public boolean existsByTitleAndIdNot(String title, MovieId id) {
        return jpaRepository.existsByTitleAndIdNot(title, id.getValue());
    }
}
