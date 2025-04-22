package com.image.processing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UIController {

    @GetMapping("/resize")
    public String resize() {
        return "resize";
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/convert")
    public String showConvertPage() {
        return "convert"; // if using Thymeleaf
    }
}
