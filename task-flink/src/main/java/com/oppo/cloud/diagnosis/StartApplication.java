package com.oppo.cloud.diagnosis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 */
@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.oppo.cloud.*",
})
@EnableScheduling
public class StartApplication {

    public static void main(String[] args) throws Exception {
        log.info("########## diagnosis starting ############");
        SpringApplication.run(StartApplication.class, args);
        log.info("########## diagnosis started ############");
    }

}


