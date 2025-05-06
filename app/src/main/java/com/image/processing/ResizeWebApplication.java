package com.image.processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {
		"com.app",                        // for all services/controllers
		"com.image.processing",          // your main module
		"com.image",  				    // backend module
})
@EnableJpaRepositories(basePackages = {
		"com.app.repository",                     // for UserDataRepository
		"com.image.processing.repository",        // for ImageRepository
})
@EntityScan(basePackages = {
		"com.app.entity",                         // for UserData
		"com.image.processing.entity"             // for Image
})
public class ResizeWebApplication {
	public static void main(String[] args) {
		SpringApplication.run(ResizeWebApplication.class, args);
	}
}

