package com.example.travelProj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TravelProjApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelProjApplication.class, args);
	}

}
