package com.aistarter.ai.service;

import com.aistarter.ai.dto.ChatRequest;
import com.aistarter.ai.memory.ChatMemory;
import com.aistarter.prompt.entity.PromptType;
import com.aistarter.prompt.service.PromptFallbacks;
import com.aistarter.prompt.service.PromptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ChatMemory chatMemory;
    @Mock
    private PromptService promptService;
    @InjectMocks
    private ChatService chatService;

    @Test
    void chatShouldReturnModelResponse() {
        when(chatMemory.getHistory(anyString())).thenReturn(Collections.emptyList());
        when(promptService.renderOptional(eq(PromptFallbacks.KEY_CHAT), eq(PromptType.system), any(Map.class)))
                .thenReturn(Optional.of("system prompt"));

        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("你好，我是AI助手。");

        ChatRequest request = new ChatRequest();
        request.setMessage("你好");
        var response = chatService.chat(request);
        assertEquals("你好，我是AI助手。", response.getContent());
        verify(chatMemory).add("default", "user", "你好");
        verify(chatMemory).add("default", "assistant", "你好，我是AI助手。");
    }
}
