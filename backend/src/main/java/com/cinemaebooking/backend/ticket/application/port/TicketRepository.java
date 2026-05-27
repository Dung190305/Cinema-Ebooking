package com.cinemaebooking.backend.ticket.application.port;

import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import com.cinemaebooking.backend.ticket.domain.valueObject.TicketId;
import java.util.Optional;
import java.util.List;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    Optional<Ticket> findById(TicketId id);
    Optional<Ticket> findByTicketCode(String ticketCode);
    List<Ticket> findByBookingId(Long bookingId);
    List<Ticket> findByBookingCode(String bookingCode);
    boolean existsActiveTicketsForSeats(List<Long> seatIds);
    List<Ticket> findAllByIds(List<Long> ids);
}
