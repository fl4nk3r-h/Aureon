package com.aureon.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AureonApplication {
    public static void main(String[] args) {
        SpringApplication.run(AureonApplication.class, args);
    }
}
