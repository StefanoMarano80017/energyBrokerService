package com.sch.energyBrokerService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EnergyBrokerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnergyBrokerServiceApplication.class, args);
	}

}
