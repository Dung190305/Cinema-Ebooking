package com.cinemaebooking.backend.review.application.dto;

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

    private List<String> censoredWords;

    @JsonProperty("profanity_count")
    private int profanityCount;

    @JsonProperty("profanity_ratio")
    private double profanityRatio;

    @JsonProperty("final_output")
    private String finalOutput;

    @JsonProperty("process_time")
    private double processTime;
}