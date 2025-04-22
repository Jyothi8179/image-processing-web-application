package com.image.processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {
		"com.app",
		"com.image.*"
})
public class ResizeWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResizeWebApplication.class, args);
	}

}
