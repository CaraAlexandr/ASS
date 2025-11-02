package com.scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DistributedMonolithApplication {
    public static void main(String[] args) {
        SpringApplication.run(DistributedMonolithApplication.class, args);
    }
}

