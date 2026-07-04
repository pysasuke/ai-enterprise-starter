package com.aistarter.workflow.router;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AgentRouteLlmClassifier {

    private static final String SYSTEM_PROMPT = """
            You are a routing classifier. Given a user question, reply with exactly one word:
            CHAT, RAG, or DATABASE.
            CHAT = general conversation.
            RAG = questions about uploaded documents, policies, knowledge base.
            DATABASE = SQL, schema, indexes, slow queries, table design.
            Reply with only the single word, no punctuation.
            """;

    private final ChatClient.Builder chatClientBuilder;

    public AgentRouteDecision classify(String question) {
        try {
            String raw = chatClientBuilder.build()
                    .prompt()
                    .system(SYSTEM_PROMPT)
                    .user(question)
                    .call()
                    .content();
            return parseDecision(raw);
        } catch (Exception ex) {
            return new AgentRouteDecision(AgentType.CHAT, RouteMethod.FALLBACK);
        }
    }

    private AgentRouteDecision parseDecision(String raw) {
        if (raw == null || raw.isBlank()) {
            return new AgentRouteDecision(AgentType.CHAT, RouteMethod.FALLBACK);
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("DATABASE")) {
            return new AgentRouteDecision(AgentType.DATABASE, RouteMethod.LLM);
        }
        if (normalized.contains("RAG")) {
            return new AgentRouteDecision(AgentType.RAG, RouteMethod.LLM);
        }
        if (normalized.contains("CHAT")) {
            return new AgentRouteDecision(AgentType.CHAT, RouteMethod.LLM);
        }
        return new AgentRouteDecision(AgentType.CHAT, RouteMethod.FALLBACK);
    }
}
