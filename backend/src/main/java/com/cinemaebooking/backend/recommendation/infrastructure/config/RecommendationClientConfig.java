package com.cinemaebooking.backend.recommendation.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RecommendationClientConfig {

    @Bean
    public RestTemplate recommendationRestTemplate() {
        return new RestTemplate();
    }
}