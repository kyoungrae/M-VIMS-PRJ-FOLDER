package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableFeignClients(basePackages = "com.vims.fmsClient")
public class VimsInspectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(VimsInspectionApplication.class, args);
	}

}
