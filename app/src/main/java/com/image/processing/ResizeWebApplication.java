package com.image.processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {
		"com.image.processing",       // your main app and ImageService
		"com.image.resize.service"    // backend module with ImageResizeService
})
public class ResizeWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResizeWebApplication.class, args);
	}

}
