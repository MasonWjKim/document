package com.example.style.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/createForm")
    public String createForm() {
        return "createForm";
    }

    @GetMapping("/inquiry")
    public String inquiry() {
        return "inquiry";
    }

}
