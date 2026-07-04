package com.aistarter.ai.service;

import com.aistarter.ai.dto.ChatStreamRequest;
import com.aistarter.ai.memory.ChatMemory;
import com.aistarter.ai.stream.SseEvent;
import com.aistarter.ai.stream.StreamToolChatOrchestrator;
import com.aistarter.prompt.entity.PromptType;
import com.aistarter.prompt.service.PromptFallbacks;
import com.aistarter.prompt.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class StreamChatService {

    private static final String DEFAULT_SESSION = "default";

    private final ChatClient.Builder chatClientBuilder;
    private final ChatMemory chatMemory;
    private final PromptService promptService;
    private final StreamToolChatOrchestrator orchestrator;

    public Flux<SseEvent> chatStream(ChatStreamRequest request) {
        String sessionId = request.getSessionId() == null || request.getSessionId().isBlank()
                ? DEFAULT_SESSION
                : request.getSessionId();

        var promptBuilder = new StringBuilder();
        for (String entry : chatMemory.getHistory(sessionId)) {
            promptBuilder.append(entry).append("\n");
        }
        promptBuilder.append("user:").append(request.getMessage());

        String systemPrompt = promptService.renderOptional(PromptFallbacks.KEY_CHAT, PromptType.system, Map.of())
                .orElse(null);
        String userPrompt = promptBuilder.toString();
        String conversationId = SseEvent.newConversationId();

        StringBuilder assistantText = new StringBuilder();
        AtomicBoolean completed = new AtomicBoolean(false);

        return orchestrator.stream(
                        conversationId,
                        chatClientBuilder,
                        systemPrompt,
                        userPrompt,
                        request.isEnableTools(),
                        Map.of())
                .doOnNext(event -> {
                    if ("chunk".equals(event.type())) {
                        Object content = event.payload().get("content");
                        if (content != null) {
                            assistantText.append(content);
                        }
                    }
                    if ("done".equals(event.type())) {
                        completed.set(true);
                    }
                })
                .doOnComplete(() -> {
                    if (!completed.get()) {
                        return;
                    }
                    chatMemory.add(sessionId, "user", request.getMessage());
                    if (!assistantText.isEmpty()) {
                        chatMemory.add(sessionId, "assistant", assistantText.toString());
                    }
                });
    }
}
