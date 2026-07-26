package com.SpeakMate.Ai.friend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FriendApplication {

	public static void main(String[] args) {

		SpringApplication application =
				new SpringApplication(FriendApplication.class);

		application.setApplicationStartup(
				new BufferingApplicationStartup(2048)
		);

		application.run(args);
	}
}