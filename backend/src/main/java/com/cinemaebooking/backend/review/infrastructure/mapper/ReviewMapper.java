package com.cinemaebooking.backend.review.infrastructure.mapper;

import com.cinemaebooking.backend.review.domain.model.Review;
import com.cinemaebooking.backend.review.infrastructure.persistence.entity.ReviewJpaEntity;

public interface ReviewMapper {

    Review toDomain(ReviewJpaEntity entity);

    ReviewJpaEntity toEntity(Review domain);

    void updateEntity(Review source, ReviewJpaEntity target);
}
