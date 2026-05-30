package com.cinemaebooking.backend;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepositoryImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling
@EnableJpaRepositories(
		basePackages = "com.cinemaebooking.backend",
		repositoryBaseClass = SoftDeleteJpaRepositoryImpl.class
)
@EnableJpaAuditing
@SpringBootApplication
public class BackendApplication {
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
	}

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}