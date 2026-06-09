package com.cinemaebooking.backend.review.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponseDto {

    private boolean valid;

    private String label;

    private String cleanedText;

    private String finalOutput;

    private String finalDecision;

    private boolean spoiler;

    private double spoilerConf;

    private List<String> censoredWords;

    private int profanityCount;

    private double profanityRatio;

    private double processTime;
}
