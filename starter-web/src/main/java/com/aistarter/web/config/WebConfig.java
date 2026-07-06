package com.aistarter.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/chat").setViewName("forward:/index.html");
        registry.addViewController("/rag").setViewName("forward:/index.html");
        registry.addViewController("/prompts").setViewName("forward:/index.html");
        registry.addViewController("/tools").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
    }
}
