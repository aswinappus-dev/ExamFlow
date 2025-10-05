package com.examhall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExamController {

    @GetMapping("/")
    public String homePage() {
        return "Login";  // This will load index.html from templates/
    }
}
