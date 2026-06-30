package com.aistarter.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiAutoConfiguration {

    @Bean
    public ChatClient.Builder chatClientBuilder(org.springframework.ai.chat.model.ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
