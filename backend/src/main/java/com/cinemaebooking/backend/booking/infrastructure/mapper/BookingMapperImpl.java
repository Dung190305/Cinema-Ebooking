package com.cinemaebooking.backend.booking.infrastructure.mapper;

import com.cinemaebooking.backend.booking.domain.model.Booking;
import com.cinemaebooking.backend.booking.domain.valueObject.BookingId;
import com.cinemaebooking.backend.booking.infrastructure.persistence.entity.BookingJpaEntity;
import com.cinemaebooking.backend.ticket.infrastructure.mapper.TicketMapper;
import com.cinemaebooking.backend.booking_combo.infrastructure.mapper.BookingComboMapper;
import com.cinemaebooking.backend.booking_coupon.infrastructure.mapper.BookingCouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * BookingMapperImpl - Chuyển đổi giữa Domain Model và JPA Entity.
 * Sử dụng các Mapper thành phần để xử lý các object con.
 */
@Component
@RequiredArgsConstructor
public class BookingMapperImpl implements BookingMapper {

    private final TicketMapper ticketMapper;
    private final BookingComboMapper comboMapper;
    private final BookingCouponMapper couponMapper;

    @Override
    public Booking toDomain(BookingJpaEntity entity) {
        if (entity == null) return null;

        return Booking.builder()
                .id(BookingId.ofNullable(entity.getId()))
                .bookingCode(entity.getBookingCode())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .showtimeId(entity.getShowtimeId())
                .movieTitle(entity.getMovieTitle())
                .cinemaName(entity.getCinemaName())
                .roomName(entity.getRoomName())
                .showtimeStartTime(entity.getShowtimeStartTime())
                .createdAt(entity.getCreatedAt())
                .expiredAt(entity.getExpiredAt())
                .paidAt(entity.getPaidAt())
                .status(entity.getStatus())
                .totalTicketPrice(entity.getTotalTicketPrice())
                .totalComboPrice(entity.getTotalComboPrice())
                .tierDiscountAmount(entity.getTierDiscountAmount())
                .couponDiscountAmount(entity.getCouponDiscountAmount())
                .membershipTierName(entity.getMembershipTierName())
                .membershipDiscountPercent(entity.getMembershipDiscountPercent())
                .finalAmount(entity.getFinalAmount())
                .tickets(entity.getTickets() != null ?
                        entity.getTickets().stream()
                                .map(ticketMapper::toDomain)
                                .collect(Collectors.toList()) : new ArrayList<>())
                .combos(entity.getCombos() != null ?
                        entity.getCombos().stream()
                                .map(comboMapper::toDomain)
                                .collect(Collectors.toList()) : new ArrayList<>())
                .coupon(entity.getCoupon() != null
                        ? couponMapper.toDomain(entity.getCoupon())
                        : null)
                .build();
    }

    @Override
    public BookingJpaEntity toEntity(Booking domain) {
        if (domain == null) return null;

        BookingJpaEntity entity = BookingJpaEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .bookingCode(domain.getBookingCode())
                .showtimeId(domain.getShowtimeId())
                .movieTitle(domain.getMovieTitle())
                .cinemaName(domain.getCinemaName())
                .roomName(domain.getRoomName())
                .showtimeStartTime(domain.getShowtimeStartTime())
                .totalTicketPrice(domain.getTotalTicketPrice())
                .totalComboPrice(domain.getTotalComboPrice())
                .tierDiscountAmount(domain.getTierDiscountAmount())
                .couponDiscountAmount(domain.getCouponDiscountAmount())
                .membershipTierName(domain.getMembershipTierName())
                .membershipDiscountPercent(domain.getMembershipDiscountPercent())
                .finalAmount(domain.getFinalAmount())
                .status(domain.getStatus())
                .expiredAt(domain.getExpiredAt())
                .paidAt(domain.getPaidAt())
                .tickets(new ArrayList<>())
                .combos(new ArrayList<>())
                .build();

        if (domain.getTickets() != null) {
            domain.getTickets()
                    .forEach(t -> entity.addTicket(ticketMapper.toEntity(t)));
        }

        if (domain.getCombos() != null) {
            domain.getCombos()
                    .forEach(c -> entity.addCombo(comboMapper.toEntity(c)));
        }

        if (domain.getCoupon() != null) {
            entity.setCoupon(couponMapper.toEntity(domain.getCoupon()));
        }

        return entity;
    }

    @Override
    public void updateEntity(Booking source, BookingJpaEntity target) {

        target.setStatus(source.getStatus());
        target.setPaidAt(source.getPaidAt());

        target.setTotalTicketPrice(source.getTotalTicketPrice());
        target.setTotalComboPrice(source.getTotalComboPrice());
        target.setTierDiscountAmount(source.getTierDiscountAmount());
        target.setCouponDiscountAmount(source.getCouponDiscountAmount());
        target.setMembershipTierName(source.getMembershipTierName());
        target.setMembershipDiscountPercent(source.getMembershipDiscountPercent());
        target.setFinalAmount(source.getFinalAmount());
    }
}