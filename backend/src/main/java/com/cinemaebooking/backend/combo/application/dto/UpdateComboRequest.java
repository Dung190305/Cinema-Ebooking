package com.cinemaebooking.backend.combo.application.dto;

import com.cinemaebooking.backend.combo.domain.enums.ComboStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComboRequest {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private ComboStatus status;
}
