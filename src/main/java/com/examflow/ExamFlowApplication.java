package com.examflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExamFlowApplication {
	public String home() {
        return "Hello, Spring Boot is running!";
	}
	public static void main(String[] args) {
		SpringApplication.run(ExamFlowApplication.class, args);
	}

}
