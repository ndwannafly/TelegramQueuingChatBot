package com.example.telegramqueueingchatbot;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin

public class Demo {
    @GetMapping("/")
    public String check() {
        return "Run success";
    }
}
