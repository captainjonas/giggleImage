package com.giggle.image;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GiggleImageApplication {
    public static void main(String[] args) {
        SpringApplication.run(GiggleImageApplication.class, args);
    }
} 