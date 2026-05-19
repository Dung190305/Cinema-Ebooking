package com.cinemaebooking.backend.review.infrastructure.mapper;

import com.cinemaebooking.backend.infrastructure.mapper.BaseMapper;
import com.cinemaebooking.backend.review.domain.model.Review;
import com.cinemaebooking.backend.review.infrastructure.persistence.entity.ReviewJpaEntity;

public interface ReviewMapper extends BaseMapper<Review, ReviewJpaEntity> {
    void updateEntity(Review source, ReviewJpaEntity target);
}
