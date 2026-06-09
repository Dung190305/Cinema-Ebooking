package com.cinemaebooking.backend.dashboard.infrastructure.service;

import com.cinemaebooking.backend.cinema.domain.enums.CinemaStatus;
import com.cinemaebooking.backend.dashboard.application.dto.*;
import com.cinemaebooking.backend.dashboard.application.service.DashboardService;
import com.cinemaebooking.backend.dashboard.infrastructure.persistence.repository.DashboardCinemaJpaRepository;
import com.cinemaebooking.backend.dashboard.infrastructure.persistence.repository.DashboardJpaRepository;
import com.cinemaebooking.backend.dashboard.infrastructure.persistence.repository.DashboardShowtimeJpaRepository;
import com.cinemaebooking.backend.showtime.domain.enums.ShowtimeStatus;
import com.cinemaebooking.backend.showtime.infrastructure.persistence.entity.ShowtimeJpaEntity;
import com.cinemaebooking.backend.showtime_seat.domain.enums.ShowtimeSeatStatus;
import com.cinemaebooking.backend.showtime_seat.infrastructure.persistence.entity.ShowtimeSeatJpaEntity;
import com.cinemaebooking.backend.showtime_seat.infrastructure.persistence.repository.ShowtimeSeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final DashboardJpaRepository dashboardJpaRepository;
    private final DashboardShowtimeJpaRepository showtimeJpaRepository;
    private final DashboardCinemaJpaRepository cinemaJpaRepository;
    private final ShowtimeSeatJpaRepository showtimeSeatJpaRepository;

    private static final ZoneId TIMEZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Value("${app.timezone.db:Asia/Ho_Chi_Minh}")
    private String dbTimezone;

    @Override
    public DashboardSummaryDto getDashboardSummary(Long cinemaId) {
        LocalDate today = LocalDate.now(TIMEZONE);
        LocalDate yesterday = today.minusDays(1);

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = todayStart;

        // ── 1. Today's Revenue ───────────────────────────────────────────────────
        BigDecimal todayRevenue = dashboardJpaRepository.sumTodayRevenue(todayStart, todayEnd, cinemaId);
        BigDecimal yesterdayRevenue = dashboardJpaRepository.sumYesterdayRevenue(yesterdayStart, yesterdayEnd, cinemaId);
        Double revenueChange = calcChange(todayRevenue, yesterdayRevenue);

        MetricCardDto revenueCard = MetricCardDto.builder()
                .value(todayRevenue)
                .changePercent(revenueChange)
                .label("Doanh thu hôm nay")
                .prefix("₫")
                .build();

        // ── 2. Tickets Sold ─────────────────────────────────────────────────────
        Long todayBookings = dashboardJpaRepository.countTodayBookings(todayStart, todayEnd, cinemaId);
        Long yesterdayBookings = dashboardJpaRepository.countYesterdayBookings(yesterdayStart, yesterdayEnd, cinemaId);
        Double ticketsChange = calcChange(todayBookings.doubleValue(), yesterdayBookings.doubleValue());

        MetricCardDto ticketsCard = MetricCardDto.builder()
                .value(BigDecimal.valueOf(todayBookings))
                .changePercent(ticketsChange)
                .label("Vé đã bán")
                .suffix(" vé")
                .build();

        // ── 3. Occupancy Rate ───────────────────────────────────────────────────
        Long todayBookedSeats = dashboardJpaRepository.countTodayBookedSeats(todayStart, todayEnd, cinemaId);
        Long yesterdayBookedSeats = dashboardJpaRepository.countYesterdayBookedSeats(yesterdayStart, yesterdayEnd, cinemaId);

        Long todayAvailable = dashboardJpaRepository.countTotalAvailableSeats(
                todayStart, todayEnd, cinemaId);
        Long yesterdayAvailable = dashboardJpaRepository.countYesterdayAvailableSeats(
                yesterdayStart, cinemaId);

        Double todayOccupancy = todayAvailable != null && todayAvailable > 0
                ? todayBookedSeats.doubleValue() / todayAvailable.doubleValue() * 100.0
                : 0.0;
        Double yesterdayOccupancy = yesterdayAvailable != null && yesterdayAvailable > 0
                ? yesterdayBookedSeats.doubleValue() / yesterdayAvailable.doubleValue() * 100.0
                : 0.0;

        MetricCardDto occupancyCard = MetricCardDto.builder()
                .value(BigDecimal.valueOf(todayOccupancy).setScale(1, RoundingMode.HALF_UP))
                .changePercent(calcChange(todayOccupancy, yesterdayOccupancy))
                .label("Tỷ lệ lấp đầy")
                .suffix("%")
                .build();

        // ── 4. New Users ─────────────────────────────────────────────────────────
        Long todayUsers = dashboardJpaRepository.countNewUsersToday(todayStart, todayEnd);
        Long yesterdayUsers = dashboardJpaRepository.countNewUsersYesterday(yesterdayStart, yesterdayEnd);
        Double usersChange = calcChange(todayUsers.doubleValue(), yesterdayUsers.doubleValue());

        MetricCardDto usersCard = MetricCardDto.builder()
                .value(BigDecimal.valueOf(todayUsers))
                .changePercent(usersChange)
                .label("Người dùng mới")
                .suffix(" người")
                .build();

        // ── 5. Hourly Revenue ────────────────────────────────────────────────────
        List<HourlyRevenueDto> hourlyRevenue = buildHourlyRevenue(todayStart, todayEnd, cinemaId, dbTimezone);

        // ── 6. Top 5 Movies ──────────────────────────────────────────────────────
        List<TopMovieDto> topMovies = buildTopMovies(todayStart, todayEnd, cinemaId);

        // ── 7. Revenue Structure ─────────────────────────────────────────────────
        BigDecimal ticketRevenue = dashboardJpaRepository.sumTodayTicketRevenue(todayStart, todayEnd, cinemaId);
        BigDecimal fnbRevenue = dashboardJpaRepository.sumTodayFnbRevenue(todayStart, todayEnd, cinemaId);
        RevenueStructureDto structure = buildRevenueStructure(ticketRevenue, fnbRevenue);

        // ── 8. Cinema Statuses (always show ALL cinemas) ────────────────────────
        List<CinemaStatusDto> cinemaStatuses = buildCinemaStatuses(null);

        // ── 9. Upcoming Showtimes ───────────────────────────────────────────────
        List<UpcomingShowtimeDto> upcomingShowtimes = buildUpcomingShowtimes(cinemaId);

        return DashboardSummaryDto.builder()
                .todayRevenue(revenueCard)
                .ticketsSold(ticketsCard)
                .occupancyRate(occupancyCard)
                .newUsers(usersCard)
                .hourlyRevenue(hourlyRevenue)
                .topMovies(topMovies)
                .revenueStructure(structure)
                .cinemaStatuses(cinemaStatuses)
                .upcomingShowtimes(upcomingShowtimes)
                .build();
    }

    private Double calcChange(Double current, Double previous) {
        if (previous == null || previous == 0.0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100.0;
    }

    private Double calcChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private List<HourlyRevenueDto> buildHourlyRevenue(
            LocalDateTime dayStart, LocalDateTime dayEnd, Long cinemaId, String dbTimezone) {

        List<Object[]> raw = dashboardJpaRepository.sumHourlyRevenue(dayStart, dayEnd, cinemaId, dbTimezone);
        List<HourlyRevenueDto> result = new ArrayList<>();

        for (int h = 0; h < 24; h++) {
            final int hour = h;
            BigDecimal revenue = raw.stream()
                    .filter(r -> ((Number) r[0]).intValue() == hour)
                    .findFirst()
                    .map(r -> (BigDecimal) r[1])
                    .orElse(BigDecimal.ZERO);
            result.add(HourlyRevenueDto.builder()
                    .hour(hour)
                    .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                    .build());
        }
        return result;
    }

    private List<TopMovieDto> buildTopMovies(
            LocalDateTime dayStart, LocalDateTime dayEnd, Long cinemaId) {

        List<Object[]> raw = dashboardJpaRepository.findTop5Movies(dayStart, dayEnd, cinemaId);
        List<TopMovieDto> result = new ArrayList<>();
        int rank = 1;
        for (Object[] row : raw) {
            if (rank > 5) break;
            String title = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            BigDecimal revenue = (BigDecimal) row[2];
            result.add(TopMovieDto.builder()
                    .movieTitle(title)
                    .ticketCount(count)
                    .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                    .rank(rank++)
                    .build());
        }
        return result;
    }

    private RevenueStructureDto buildRevenueStructure(
            BigDecimal ticketRevenue, BigDecimal fnbRevenue) {

        BigDecimal total = ticketRevenue.add(fnbRevenue);
        Double ticketPct = total.compareTo(BigDecimal.ZERO) > 0
                ? ticketRevenue.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;
        Double fnbPct = total.compareTo(BigDecimal.ZERO) > 0
                ? fnbRevenue.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return RevenueStructureDto.builder()
                .ticketRevenue(ticketRevenue)
                .fnbRevenue(fnbRevenue)
                .totalRevenue(total)
                .ticketPercent(Math.round(ticketPct * 10.0) / 10.0)
                .fnbPercent(Math.round(fnbPct * 10.0) / 10.0)
                .build();
    }

    private List<CinemaStatusDto> buildCinemaStatuses(Long cinemaId) {
        List<com.cinemaebooking.backend.cinema.infrastructure.persistence.entity.CinemaJpaEntity> cinemas;
        if (cinemaId != null) {
            cinemas = cinemaJpaRepository.findByIdAndDeletedFalse(cinemaId).stream().toList();
        } else {
            cinemas = cinemaJpaRepository.findByDeletedFalse();
        }

        return cinemas.stream()
                .map(c -> CinemaStatusDto.builder()
                        .cinemaId(c.getId())
                        .cinemaName(c.getName())
                        .status(c.getStatus() == CinemaStatus.ACTIVE ? "ACTIVE" : "INACTIVE")
                        .maintenanceNote(null)
                        .build())
                .toList();
    }

    private List<UpcomingShowtimeDto> buildUpcomingShowtimes(Long cinemaId) {
        Instant now = Instant.now();
        Instant cutoff = now.plusSeconds(7200); // +2 hours

        List<ShowtimeJpaEntity> showtimes =
                showtimeJpaRepository.findUpcomingShowtimes(now, cutoff, cinemaId);

        return showtimes.stream().limit(7).map(s -> {
            List<ShowtimeSeatJpaEntity> seats = showtimeSeatJpaRepository.findByShowtimeId(s.getId());

            long totalActive = seats.stream().filter(ShowtimeSeatJpaEntity::isActive).count();
            long bookedCount = seats.stream()
                    .filter(seat -> seat.getStatus() == ShowtimeSeatStatus.BOOKED)
                    .count();

            String ratio = totalActive > 0
                    ? bookedCount + "/" + totalActive
                    : "0/0";

            LocalDateTime localStartTime = s.getStartTime()
                    .atZone(TIMEZONE)
                    .toLocalDateTime();

            return UpcomingShowtimeDto.builder()
                    .showtimeId(s.getId())
                    .movieTitle(s.getMovie().getTitle())
                    .screenRoom(s.getRoom().getName())
                    .cinemaBranch(s.getRoom().getCinema().getName())
                    .startTime(localStartTime)
                    .bookedSeats((int) bookedCount)
                    .totalSeats((int) totalActive)
                    .bookedRatio(ratio)
                    .build();
        }).toList();
    }
}
