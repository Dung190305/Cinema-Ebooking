package com.cinemaebooking.backend.review.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApiResponseDto {

    private boolean valid;
    private String sentiment;

    @JsonProperty("final_decision")
    private String finalDecision;

    private String action;

    @JsonProperty("cleaned_text")
    private String cleanedText;

    @JsonProperty("sentiment_score")
    private double sentimentScore;

    @JsonProperty("is_spoiler")
    private boolean isSpoiler;

    @JsonProperty("spoiler_conf")
    private double spoilerConf;

    @JsonProperty("process_time")
    private double processTime;
}