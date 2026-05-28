package com.cinemaebooking.backend.ticket.infrastructure.adapter;

import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.showtime_seat.infrastructure.persistence.repository.ShowtimeSeatJpaRepository;
import com.cinemaebooking.backend.ticket.domain.enums.TicketStatus;
import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import com.cinemaebooking.backend.ticket.domain.valueObject.TicketId;
import com.cinemaebooking.backend.ticket.infrastructure.mapper.TicketMapper;
import com.cinemaebooking.backend.ticket.infrastructure.persistence.entity.TicketJpaEntity;
import com.cinemaebooking.backend.ticket.infrastructure.persistence.repository.TicketJpaRepository;
import com.cinemaebooking.backend.ticket.application.port.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TicketRepositoryImpl implements TicketRepository {

    private final TicketJpaRepository ticketJpaRepository;
    private final TicketMapper ticketMapper;
    private final ShowtimeSeatJpaRepository showtimeSeatJpaRepository;
    private final BookingJpaRepository bookingJpaRepository;

    @Override
    public Ticket save(Ticket ticket) {
        TicketJpaEntity entity;

        if (ticket.getId() != null) {
            entity = ticketJpaRepository.findById(ticket.getId().getValue())
                    .orElseThrow();
            entity.setStatus(ticket.getStatus());
            entity.setCheckedInAt(ticket.getCheckedInAt());

            if (ticket.getStatus() == TicketStatus.CANCELLED) {
                entity.softDelete();
            }
        } else {
            entity = ticketMapper.toEntity(ticket);
            entity.setShowtimeSeat(showtimeSeatJpaRepository.getReferenceById(ticket.getShowtimeSeatId()));
            entity.setBooking(bookingJpaRepository.getReferenceById(ticket.getBookingId()));
        }

        return ticketMapper.toDomain(ticketJpaRepository.save(entity));
    }

    @Override
    public Optional<Ticket> findById(TicketId id) {
        return ticketJpaRepository.findById(id.getValue())
                .map(ticketMapper::toDomain);
    }

    @Override
    public Optional<Ticket> findByTicketCode(String ticketCode) {
        return ticketJpaRepository.findByTicketCodeAndDeletedAtIsNull(ticketCode)
                .map(ticketMapper::toDomain);
    }

    @Override
    public List<Ticket> findByBookingId(Long bookingId) {
        return ticketJpaRepository.findAllByBookingIdAndDeletedAtIsNull(bookingId)
                .stream()
                .map(ticketMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findAllByIds(List<Long> ids) {
        return ticketJpaRepository.findAllByIdInAndDeletedAtIsNull(ids)
                .stream()
                .map(ticketMapper::toDomain)
                .collect(Collectors.toList());
    }
}
