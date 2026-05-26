package com.kmj5004.hdljudge;

import org.springframework.boot.SpringApplication;





public class TestHdlJudgeApplication {

	public static void main(String[] args) {
		SpringApplication.from(HdlJudgeApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
