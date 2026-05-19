package com.cinemaebooking.backend.review.infrastructure.mapper;

import com.cinemaebooking.backend.review.domain.model.Review;
import com.cinemaebooking.backend.review.domain.valueobject.ReviewId;
import com.cinemaebooking.backend.review.infrastructure.persistence.entity.ReviewJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public Review toDomain(ReviewJpaEntity entity) {
        if (entity == null) return null;

        return Review.builder()
                .id(ReviewId.ofNullable(entity.getId()))
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .movieId(entity.getMovieId())
                .bookingId(entity.getBooking() != null ? entity.getBooking().getId() : null)
                .rating(entity.getRating())
                .comment(entity.getComment())
                .sentiment(entity.getSentiment())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .editedAt(entity.getEditedAt())
                .build();
    }

    @Override
    public ReviewJpaEntity toEntity(Review domain) {
        if (domain == null) return null;

        return ReviewJpaEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .movieId(domain.getMovieId())
                .rating(domain.getRating())
                .comment(domain.getComment())
                .sentiment(domain.getSentiment())
                .status(domain.getStatus())
                .editedAt(domain.getEditedAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    @Override
    public void updateEntity(Review source, ReviewJpaEntity target) {
        target.setRating(source.getRating());
        target.setComment(source.getComment());
        target.setSentiment(source.getSentiment());
        target.setStatus(source.getStatus());
        target.setEditedAt(source.getEditedAt());
    }
}
