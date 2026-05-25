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
public class RevenueStructureDto {
    private BigDecimal ticketRevenue;
    private BigDecimal fnbRevenue;
    private BigDecimal totalRevenue;
    private Double ticketPercent;
    private Double fnbPercent;
}
