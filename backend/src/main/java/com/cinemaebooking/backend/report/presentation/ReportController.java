package com.cinemaebooking.backend.report.presentation;

import com.cinemaebooking.backend.report.application.dto.*;
import com.cinemaebooking.backend.report.application.usecase.GetMoviePerformanceUseCase;
import com.cinemaebooking.backend.report.application.usecase.GetRevenueOverviewUseCase;
import com.cinemaebooking.backend.report.application.usecase.GetRevenueTrendUseCase;
import com.cinemaebooking.backend.report.application.usecase.GetCinemaPerformanceUseCase;
import com.cinemaebooking.backend.report.application.usecase.GetComboSalesReportUseCase;
import com.cinemaebooking.backend.report.application.usecase.GetGoldenHoursUseCase;
import com.cinemaebooking.backend.report.application.usecase.GetPaymentMethodReportUseCase;
import com.cinemaebooking.backend.report.application.usecase.GetPromotionEffectivenessUseCase;
import com.cinemaebooking.backend.report.application.usecase.GetRetentionReportUseCase;
import com.cinemaebooking.backend.report.application.usecase.GetRoomPerformanceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final GetRevenueOverviewUseCase getRevenueOverviewUseCase;
    private final GetRevenueTrendUseCase getRevenueTrendUseCase;
    private final GetMoviePerformanceUseCase getMoviePerformanceUseCase;
    private final GetComboSalesReportUseCase getComboSalesReportUseCase;
    private final GetPaymentMethodReportUseCase getPaymentMethodReportUseCase;
    private final GetCinemaPerformanceUseCase getCinemaPerformanceUseCase;
    private final GetRoomPerformanceUseCase getRoomPerformanceUseCase;
    private final GetGoldenHoursUseCase getGoldenHoursUseCase;
    private final GetRetentionReportUseCase getRetentionReportUseCase;
    private final GetPromotionEffectivenessUseCase getPromotionEffectivenessUseCase;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public RevenueOverviewResponse getOverview(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) Long movieId
    ) {
        return getRevenueOverviewUseCase.execute(
                fromDate,
                toDate,
                cinemaId,
                movieId
        );
    }

    @GetMapping("/revenue-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RevenueTrendPointResponse> getRevenueTrend(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) Long movieId,

            @RequestParam(defaultValue = "DAY") ReportGroupBy groupBy
    ) {
        return getRevenueTrendUseCase.execute(
                fromDate,
                toDate,
                cinemaId,
                movieId,
                groupBy
        );
    }

    @GetMapping("/movie-performance")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MoviePerformanceResponse> getMoviePerformance(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) Long movieId
    ) {
        return getMoviePerformanceUseCase.execute(
                fromDate,
                toDate,
                cinemaId,
                movieId
        );
    }

    @GetMapping("/combo-sales")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ComboSalesReportResponse> getComboSales(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) Long movieId
    ) {
        return getComboSalesReportUseCase.execute(fromDate, toDate, cinemaId, movieId);
    }

    @GetMapping("/payment-methods")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PaymentMethodReportResponse> getPaymentMethods(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) Long movieId
    ) {
        return getPaymentMethodReportUseCase.execute(fromDate, toDate, cinemaId, movieId);
    }

    @GetMapping("/cinema-performance")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CinemaPerformanceResponse> getCinemaPerformance(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) Long movieId
    ) {
        return getCinemaPerformanceUseCase.execute(fromDate, toDate, cinemaId, movieId);
    }

    @GetMapping("/room-performance")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RoomPerformanceResponse> getRoomPerformance(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) Long movieId
    ) {
        return getRoomPerformanceUseCase.execute(fromDate, toDate, cinemaId, movieId);
    }

    @GetMapping("/golden-hours")
    @PreAuthorize("hasRole('ADMIN')")
    public List<GoldenHourResponse> getGoldenHours(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) Long movieId
    ) {
        return getGoldenHoursUseCase.execute(fromDate, toDate, cinemaId, movieId);
    }

    @GetMapping("/retention")
    @PreAuthorize("hasRole('ADMIN')")
    public RetentionReportResponse getRetention(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) Long movieId
    ) {
        return getRetentionReportUseCase.execute(fromDate, toDate, cinemaId, movieId);
    }

    @GetMapping("/promotion-effectiveness")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PromotionEffectivenessResponse> getPromotionEffectiveness(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) Long movieId
    ) {
        return getPromotionEffectivenessUseCase.execute(fromDate, toDate, cinemaId, movieId);
    }
}