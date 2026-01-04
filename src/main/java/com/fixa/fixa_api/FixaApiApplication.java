package com.fixa.fixa_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class FixaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FixaApiApplication.class, args);
	}

}
