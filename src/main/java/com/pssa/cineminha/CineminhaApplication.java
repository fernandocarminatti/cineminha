package com.pssa.cineminha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EntityScan(basePackages = "com.pssa.cineminha.entity")
@EnableJpaRepositories(basePackages = "com.pssa.cineminha.repository")
@EnableAsync
public class CineminhaApplication {
	public static void main(String[] args) {
		SpringApplication.run(CineminhaApplication.class, args);
	}
}