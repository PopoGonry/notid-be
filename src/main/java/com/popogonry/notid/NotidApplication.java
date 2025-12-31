package com.popogonry.notid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NotidApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotidApplication.class, args);
	}
}
