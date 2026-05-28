package com.cinemaebooking.backend.booking.infrastructure.adapter;

import com.cinemaebooking.backend.booking.application.port.BookingRepository;
import com.cinemaebooking.backend.booking.domain.enums.BookingStatus;
import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.infrastructure.mapper.BookingMapper;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.booking.infrastructure.persistence.repository.BookingJpaRepository;
import com.cinemaebooking.backend.showtime_seat.infrastructure.persistence.repository.ShowtimeSeatJpaRepository;
import com.cinemaebooking.backend.ticket.domain.enums.TicketStatus;
import com.cinemaebooking.backend.ticket.domain.model.Ticket;
import com.cinemaebooking.backend.ticket.infrastructure.mapper.TicketMapper;
import com.cinemaebooking.backend.ticket.infrastructure.persistence.entity.TicketJpaEntity;
import com.cinemaebooking.backend.user.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookingRepositoryImpl implements BookingRepository {

    private final BookingJpaRepository jpaRepository;
    private final BookingMapper mapper;
    private final UserJpaRepository userJpaRepository;
    private final ShowtimeSeatJpaRepository showtimeSeatJpaRepository;
    private final TicketMapper  ticketMapper;

    @Override
    public Booking save(Booking booking) {

        BookingJpaEntity entity;

        if (booking.getId() == null) {
            entity = mapper.toEntity(booking);
            entity.setUser(
                    userJpaRepository.getReferenceById(booking.getUserId())
            );

            List<Ticket> domainTickets = booking.getTickets();
            List<TicketJpaEntity> ticketEntities = new java.util.ArrayList<>(entity.getTickets());

            for (int i = 0; i < ticketEntities.size(); i++) {
                Long seatId = domainTickets.get(i).getShowtimeSeatId();
                if (seatId != null) {
                    ticketEntities.get(i).setShowtimeSeat(
                            showtimeSeatJpaRepository.getReferenceById(seatId)
                    );
                }
            }
        } else {
            // UPDATE: booking đã tồn tại
            entity = jpaRepository.findByIdAndDeletedFalse(booking.getId().getValue())
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + booking.getId().getValue()));

            mapper.updateEntity(booking, entity);

            entity.setUser(
                    userJpaRepository.getReferenceById(booking.getUserId())
            );

            if (booking.getTickets() != null && !booking.getTickets().isEmpty()) {
                if (entity.getTickets().isEmpty()) {
                    // Tickets được tạo lần đầu khi ConfirmPayment — INSERT
                    for (Ticket t : booking.getTickets()) {
                        TicketJpaEntity ticketEntity = ticketMapper.toEntity(t);
                        if (t.getShowtimeSeatId() != null) {
                            ticketEntity.setShowtimeSeat(
                                    showtimeSeatJpaRepository.getReferenceById(t.getShowtimeSeatId())
                            );
                        }
                        entity.addTicket(ticketEntity);  // addTicket set bidirectional
                    }
                } else {
                    // Tickets đã tồn tại — UPDATE (checkIn, cancel...)
                    for (Ticket t : booking.getTickets()) {
                        TicketJpaEntity ticketEntity = entity.getTickets().stream()
                                .filter(x -> x.getId().equals(t.getId().getValue()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException(
                                        "Ticket not found in booking: " + t.getId().getValue()));
                        ticketEntity.setStatus(t.getStatus());

                        if (t.getStatus() == TicketStatus.CANCELLED
                                && ticketEntity.getDeletedAt() == null) {
                            ticketEntity.softDelete();
                        }
                    }
                }
            }
        }

        entity.getCombos().forEach(c -> c.setBooking(entity));

        if (entity.getCoupon() != null) {
            entity.getCoupon().setBooking(entity);
        }

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findById(Long id) {
        return jpaRepository.findByIdAndDeletedFalse(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<Booking> findByIdForUpdate(Long id) {
        return jpaRepository.findByIdForUpdate(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findByBookingCode(String bookingCode) {
        return jpaRepository.findByBookingCodeAndDeletedFalse(bookingCode)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Booking> findByUserId(Long userId, BookingStatus status, Pageable pageable) {
        Page<BookingJpaEntity> jpaPage = (status == null)
                ? jpaRepository.findByUserIdAndDeletedFalse(userId, pageable)
                : jpaRepository.findByUserIdAndStatusAndDeletedFalse(userId, status, pageable);

        return jpaPage.map(mapper::toDomain);
    }



    @Override
    @Transactional(readOnly = true)
    public Page<Booking> findAllForAdmin(
            Long movieId,
            BookingStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    ) {
        return jpaRepository.searchAdminBookings(
                        movieId,
                        status,
                        fromDate,
                        toDate,
                        pageable
                )
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findWithDetailsById(Long id) {
        return jpaRepository.findByIdAndDeletedFalse(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Booking> findByUserIdAndShowtimeIdAndStatus(Long userId, Long showtimeId, BookingStatus status){
        return jpaRepository.findByUserIdAndShowtimeIdAndStatus(userId,showtimeId,status).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findAllExpired(LocalDateTime now, BookingStatus status) {
        return jpaRepository.findAllByExpiredAtBeforeAndStatusAndDeletedFalse(now, status)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByBookingCode(String bookingCode) {
        return jpaRepository.existsByBookingCodeAndDeletedFalse(bookingCode);
    }
}