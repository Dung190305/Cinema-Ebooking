package com.cinemaebooking.backend.combo.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateComboRequest {

    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String imageUrl;
}
