package com.aistarter.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.aistarter")
@EntityScan(basePackages = "com.aistarter.auth.entity")
@EnableJpaRepositories(basePackages = "com.aistarter.auth.repository")
public class AiEnterpriseStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiEnterpriseStarterApplication.class, args);
    }
}
