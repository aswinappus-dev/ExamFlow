package com.examflow.examhall;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {
    @GetMapping("/")
    public String login() {
        return "Hello, Spring Boot is running!";
    }
        public String studentdashboard() {
        return "student page1";
    }
        public String invigilatordashboard() {
        return "invigilator page 1";
    }
}