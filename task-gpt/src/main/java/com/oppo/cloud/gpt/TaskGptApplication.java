package com.oppo.cloud.gpt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.oppo.cloud")
public class TaskGptApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskGptApplication.class, args);
    }
}
