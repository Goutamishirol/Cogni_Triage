package com.precisioncare.cognitriage.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HelloController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/demo-patient")
    public DemoPatient demoPatient() {
        return new DemoPatient("DEMO_0001", 74, 23, 0.5);
    }

    public record DemoPatient(
            String cohortId,
            int age,
            int mmse,
            double cdr
    ) {}
}