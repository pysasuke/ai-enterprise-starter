package com.aistarter.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Enterprise Starter API")
                        .description("Spring AI 企业开发脚手架")
                        .version("0.1.0")
                        .contact(new Contact().name("AI Enterprise Starter").url("https://github.com")));
    }
}
