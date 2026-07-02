package com.aistarter.agent.service;

import com.aistarter.agent.dto.DatabaseAgentRequest;
import com.aistarter.agent.dto.DatabaseAgentResponse;
import com.aistarter.prompt.entity.PromptType;
import com.aistarter.prompt.service.PromptFallbacks;
import com.aistarter.prompt.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DatabaseAnalyzeAgent {

    private final ChatClient.Builder chatClientBuilder;
    private final DatabaseSchemaService schemaService;
    private final PromptService promptService;

    public DatabaseAgentResponse analyze(DatabaseAgentRequest request) {
        String schema = schemaService.loadSchemaSummary();
        String indexes = schemaService.loadIndexSummary();

        String systemPrompt = promptService.render(PromptFallbacks.KEY_DATABASE_AGENT, PromptType.system, Map.of());
        String userPrompt = promptService.render(PromptFallbacks.KEY_DATABASE_AGENT, PromptType.user, Map.of(
                "question", request.getQuestion(),
                "schema", schema,
                "indexes", indexes));

        String analysis = chatClientBuilder.build()
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        return new DatabaseAgentResponse(analysis);
    }
}
