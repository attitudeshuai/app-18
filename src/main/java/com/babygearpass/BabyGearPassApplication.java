package com.babygearpass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BabyGearPassApplication {
    public static void main(String[] args) {
        SpringApplication.run(BabyGearPassApplication.class, args);
    }
}
