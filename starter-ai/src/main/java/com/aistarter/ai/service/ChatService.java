package com.aistarter.ai.service;

import com.aistarter.ai.dto.ChatRequest;
import com.aistarter.ai.dto.ChatResponse;
import com.aistarter.ai.memory.ChatMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String DEFAULT_SESSION = "default";

    private final ChatClient.Builder chatClientBuilder;
    private final ChatMemory chatMemory;

    public ChatResponse chat(ChatRequest request) {
        return chat(request, DEFAULT_SESSION);
    }

    public ChatResponse chat(ChatRequest request, String sessionId) {
        var promptBuilder = new StringBuilder();
        for (String entry : chatMemory.getHistory(sessionId)) {
            promptBuilder.append(entry).append("\n");
        }
        promptBuilder.append("user:").append(request.getMessage());

        String content = chatClientBuilder.build()
                .prompt()
                .user(promptBuilder.toString())
                .call()
                .content();

        chatMemory.add(sessionId, "user", request.getMessage());
        chatMemory.add(sessionId, "assistant", content);

        return new ChatResponse(content);
    }
}
