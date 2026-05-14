package com.kmj5004.hdljudge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan("com.kmj5004.hdljudge")
@EnableScheduling
public class HdlJudgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(HdlJudgeApplication.class, args);
	}

}
