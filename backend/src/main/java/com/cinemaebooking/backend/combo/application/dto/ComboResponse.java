package com.cinemaebooking.backend.combo.application.dto;

import com.cinemaebooking.backend.combo.domain.enums.ComboDisplayStatus;
import com.cinemaebooking.backend.combo.domain.enums.ComboStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ComboResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String imageUrl;
    private ComboStatus status;
    private ComboDisplayStatus displayStatus;
}
