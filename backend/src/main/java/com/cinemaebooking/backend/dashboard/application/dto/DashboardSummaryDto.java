package com.cinemaebooking.backend.dashboard.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDto {
    private MetricCardDto todayRevenue;
    private MetricCardDto ticketsSold;
    private MetricCardDto occupancyRate;
    private MetricCardDto newUsers;
    private List<HourlyRevenueDto> hourlyRevenue;
    private List<TopMovieDto> topMovies;
    private RevenueStructureDto revenueStructure;
    private List<CinemaStatusDto> cinemaStatuses;
    private List<UpcomingShowtimeDto> upcomingShowtimes;
}
