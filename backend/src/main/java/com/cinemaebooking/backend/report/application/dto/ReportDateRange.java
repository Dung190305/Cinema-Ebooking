package com.cinemaebooking.backend.report.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReportDateRange {

    private LocalDateTime fromDateTime;
    private LocalDateTime toDateTime;
}