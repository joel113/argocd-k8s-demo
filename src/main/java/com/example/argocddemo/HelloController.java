package com.example.argocddemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HelloController {

    @Value("${APP_ENV:local}")
    private String env;

    @GetMapping("/")
    public Map<String, String> hello() {
        return Map.of(
                "message", "Hello from ArgoCD demo",
                "env", env
        );
    }
}
