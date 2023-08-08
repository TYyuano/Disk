package com.YuAN;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Meta{
    public static void main(String[] args) {
        SpringApplication.run(Meta.class, args);
    }
}
