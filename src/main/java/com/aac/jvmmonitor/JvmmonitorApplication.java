package com.aac.jvmmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JvmmonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(JvmmonitorApplication.class, args);
    }

}
