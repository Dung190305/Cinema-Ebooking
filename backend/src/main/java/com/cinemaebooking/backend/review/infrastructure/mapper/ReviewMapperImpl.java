package com.cinemaebooking.backend.review.infrastructure.mapper;

import com.cinemaebooking.backend.review.domain.model.Review;
import com.cinemaebooking.backend.review.domain.valueobject.ReviewId;
import com.cinemaebooking.backend.review.infrastructure.persistence.entity.ReviewJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public Review toDomain(ReviewJpaEntity e) {
        if (e == null) return null;

        return Review.builder()
                .id(ReviewId.ofNullable(e.getId()))
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .movieId(e.getMovieId())
                .bookingId(e.getBooking() != null ? e.getBooking().getId() : null)
                .rating(e.getRating())
                .comment(e.getComment())
                .finalText(e.getFinalText())
                .sentiment(e.getSentiment())
                .decision(e.getDecision())
                .status(e.getStatus())
                .isSpoiler(e.isSpoiler())
                .spoilerConf(e.getSpoilerConf())
                .createdAt(e.getCreatedAt())
                .editedAt(e.getEditedAt())
                .build();
    }

    @Override
    public ReviewJpaEntity toEntity(Review d) {
        if (d == null) return null;

        return ReviewJpaEntity.builder()
                .movieId(d.getMovieId())
                .rating(d.getRating())
                .comment(d.getComment())
                .finalText(d.getFinalText())
                .sentiment(d.getSentiment())
                .decision(d.getDecision())
                .status(d.getStatus())
                .isSpoiler(d.isSpoiler())
                .spoilerConf(d.getSpoilerConf())
                .editedAt(d.getEditedAt())
                .build();
    }

    @Override
    public void updateEntity(Review source, ReviewJpaEntity target) {
        target.setRating(source.getRating());
        target.setComment(source.getComment());
        target.setFinalText(source.getFinalText());
        target.setSentiment(source.getSentiment());
        target.setDecision(source.getDecision());
        target.setStatus(source.getStatus());
        target.setSpoiler(source.isSpoiler());
        target.setSpoilerConf(source.getSpoilerConf());
        target.setEditedAt(source.getEditedAt());
    }
}
