package com.aistarter.rag.config;

import com.aistarter.rag.ocr.DashScopeOcrClient;
import com.aistarter.rag.ocr.OcrProperties;
import com.aistarter.rag.ocr.OcrService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OcrProperties.class)
public class RagOcrAutoConfiguration {

    @Bean
    OcrService ocrService(OcrProperties properties) {
        return new DashScopeOcrClient(properties);
    }
}
