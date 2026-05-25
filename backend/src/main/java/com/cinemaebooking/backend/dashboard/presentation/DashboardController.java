package com.cinemaebooking.backend.dashboard.presentation;

import com.cinemaebooking.backend.dashboard.application.dto.DashboardSummaryDto;
import com.cinemaebooking.backend.dashboard.application.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary(
            @RequestParam(required = false) Long cinemaId
    ) {
        DashboardSummaryDto summary = dashboardService.getDashboardSummary(cinemaId);
        return ResponseEntity.ok(summary);
    }
}
