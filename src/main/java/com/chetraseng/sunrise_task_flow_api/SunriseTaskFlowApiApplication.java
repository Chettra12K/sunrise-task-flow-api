package com.chetraseng.sunrise_task_flow_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SunriseTaskFlowApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SunriseTaskFlowApiApplication.class, args);
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
