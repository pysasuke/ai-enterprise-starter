package com.aistarter.rag.config;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagAutoConfiguration {

    @Bean
    public TokenTextSplitter ragTextSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(100)
                .withMinChunkLengthToEmbed(1)
                .withMaxNumChunks(10_000)
                .withKeepSeparator(true)
                .build();
    }
}
