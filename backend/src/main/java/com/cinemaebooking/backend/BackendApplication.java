package com.cinemaebooking.backend;

import com.cinemaebooking.backend.infrastructure.persistence.repository.SoftDeleteJpaRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling

@EnableJpaRepositories(
        basePackages = "com.cinemaebooking.backend",   // scan toàn bộ backend
        repositoryBaseClass = SoftDeleteJpaRepositoryImpl.class
)

@EnableJpaAuditing
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}