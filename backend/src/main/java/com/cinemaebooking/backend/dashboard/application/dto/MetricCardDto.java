package com.cinemaebooking.backend.dashboard.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricCardDto {
    private BigDecimal value;
    private Double changePercent;
    private String label;
    private String suffix;
    private String prefix;
}
