package com.naman.youtube.mini_doratube;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MiniDoratubeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniDoratubeApplication.class, args);
	}

}
