package com.aistarter.ai.service;

import com.aistarter.ai.dto.ChatRequest;
import com.aistarter.ai.dto.ChatResponse;
import com.aistarter.ai.memory.ChatMemory;
import com.aistarter.prompt.entity.PromptType;
import com.aistarter.prompt.service.PromptFallbacks;
import com.aistarter.prompt.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String DEFAULT_SESSION = "default";

    private final ChatClient.Builder chatClientBuilder;
    private final ChatMemory chatMemory;
    private final PromptService promptService;

    public ChatResponse chat(ChatRequest request) {
        return chat(request, DEFAULT_SESSION);
    }

    public ChatResponse chat(ChatRequest request, String sessionId) {
        var promptBuilder = new StringBuilder();
        for (String entry : chatMemory.getHistory(sessionId)) {
            promptBuilder.append(entry).append("\n");
        }
        promptBuilder.append("user:").append(request.getMessage());

        var spec = chatClientBuilder.build().prompt();
        promptService.renderOptional(PromptFallbacks.KEY_CHAT, PromptType.system, Map.of())
                .ifPresent(spec::system);

        String content = spec.user(promptBuilder.toString())
                .call()
                .content();

        chatMemory.add(sessionId, "user", request.getMessage());
        chatMemory.add(sessionId, "assistant", content);

        return new ChatResponse(content);
    }
}
