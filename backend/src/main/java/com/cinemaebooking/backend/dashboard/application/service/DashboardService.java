package com.cinemaebooking.backend.dashboard.application.service;

import com.cinemaebooking.backend.dashboard.application.dto.DashboardSummaryDto;

public interface DashboardService {
    DashboardSummaryDto getDashboardSummary(Long cinemaId);
}
