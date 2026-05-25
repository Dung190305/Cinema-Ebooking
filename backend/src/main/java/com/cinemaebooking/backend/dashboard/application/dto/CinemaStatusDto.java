package com.cinemaebooking.backend.dashboard.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaStatusDto {
    private Long cinemaId;
    private String cinemaName;
    private String status;
    private String maintenanceNote;
}
