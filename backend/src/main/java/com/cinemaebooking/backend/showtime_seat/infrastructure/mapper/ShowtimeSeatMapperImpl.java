package com.cinemaebooking.backend.showtime_seat.infrastructure.mapper;

import com.cinemaebooking.backend.room_layout.infrastructure.persistence.entity.RoomLayoutSeatJpaEntity;
import com.cinemaebooking.backend.room_layout.infrastructure.persistence.repository.RoomLayoutSeatJpaRepository;
import com.cinemaebooking.backend.showtime.infrastructure.persistence.entity.ShowtimeJpaEntity;
import com.cinemaebooking.backend.showtime.infrastructure.persistence.repository.ShowtimeJpaRepository;
import com.cinemaebooking.backend.showtime_seat.domain.model.ShowtimeSeat;
import com.cinemaebooking.backend.showtime_seat.domain.valueobject.ShowtimeSeatId;
import com.cinemaebooking.backend.showtime_seat.infrastructure.persistence.entity.ShowtimeSeatJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShowtimeSeatMapperImpl implements ShowtimeSeatMapper {

    private final RoomLayoutSeatJpaRepository roomLayoutSeatJpaRepository;
    private final ShowtimeJpaRepository showtimeJpaRepository;

    @Override
    public ShowtimeSeatJpaEntity toEntity(ShowtimeSeat domain) {
        RoomLayoutSeatJpaEntity seat = roomLayoutSeatJpaRepository.getReferenceById(domain.getRoomLayoutSeatId());

        ShowtimeJpaEntity showtime = showtimeJpaRepository.getReferenceById(domain.getShowtimeId());

        return ShowtimeSeatJpaEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .roomLayoutSeat(seat)
                .showtime(showtime)
                .seatNumber(domain.getSeatNumber())
                .rowIndex(domain.getRowIndex())
                .colIndex(domain.getColIndex())
                .seatTypeId(domain.getSeatTypeId())
                .coupleGroudId(domain.getCoupleGroupId())
                .active(domain.isActive())
                .price(domain.getPrice())
                .status(domain.getStatus())
                .build();
    }

    @Override
    public ShowtimeSeat toDomain(ShowtimeSeatJpaEntity entity) {

        return ShowtimeSeat.builder()
                .id(ShowtimeSeatId.ofNullable(entity.getId()))
                .showtimeId(entity.getShowtime().getId())
                .roomLayoutSeatId(entity.getRoomLayoutSeat().getId())
                .seatNumber(entity.getSeatNumber())
                .rowIndex(entity.getRowIndex())
                .colIndex(entity.getColIndex())
                .seatTypeId(entity.getSeatTypeId())
                .coupleGroupId(entity.getCoupleGroudId())
                .active(entity.isActive())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .build();
    }
}