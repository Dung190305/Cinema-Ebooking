package com.cinemaebooking.backend.showtime.infrastructure.mappers;

import com.cinemaebooking.backend.showtime.domain.model.Showtime;
import com.cinemaebooking.backend.showtime.domain.valueobject.ShowtimeId;
import com.cinemaebooking.backend.showtime.infrastructure.persistence.entity.ShowtimeJpaEntity;
import com.cinemaebooking.backend.showtime.domain.enums.Language;
import org.springframework.stereotype.Component;

@Component
public class ShowtimeMapperImpl implements ShowtimeMapper {

    @Override
    public Showtime toDomain(ShowtimeJpaEntity entity) {
        if (entity == null) return null;

        return Showtime.builder()
                .id(ShowtimeId.ofNullable(entity.getId()))
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .audioLanguage(
                        entity.getAudioLanguage() != null
                                ? entity.getAudioLanguage()
                                : null
                )
                .subtitleLanguage(
                        entity.getSubtitleLanguage() != null
                                ? entity.getSubtitleLanguage()
                                : null
                )
                .cancelled(entity.isCancelled())
                .movieId(entity.getMovie() != null ? entity.getMovie().getId() : null)
                .roomId(entity.getRoom() != null ? entity.getRoom().getId() : null)
                .roomLayoutId(entity.getRoomLayoutId())
                .formatId(entity.getFormat() != null ? entity.getFormat().getId() : null)
                .build();
    }

    @Override
    public ShowtimeJpaEntity toEntity(Showtime domain) {
        if (domain == null) return null;

        return ShowtimeJpaEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .audioLanguage(
                        domain.getAudioLanguage() != null
                                ? domain.getAudioLanguage()
                                : null
                )
                .subtitleLanguage(
                        domain.getSubtitleLanguage() != null
                                ? domain.getSubtitleLanguage()
                                : null
                )
                .roomLayoutId(domain.getRoomLayoutId())
                .cancelled(domain.isCancelled())
                .build();
    }
}