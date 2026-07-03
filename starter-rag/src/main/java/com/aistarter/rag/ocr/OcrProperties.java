package com.aistarter.rag.ocr;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.ocr")
public class OcrProperties {

    private boolean enabled = false;
    private String model = "qwen3.5-ocr";
    private String apiKey = "";
    private int minTextLength = 50;
    private int maxPages = 20;
    private int pageTimeoutSeconds = 60;
    private int documentTimeoutSeconds = 300;
}
