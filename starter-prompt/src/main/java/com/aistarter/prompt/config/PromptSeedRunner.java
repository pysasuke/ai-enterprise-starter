package com.aistarter.prompt.config;

import com.aistarter.prompt.entity.PromptType;
import com.aistarter.prompt.service.PromptFallbacks;
import com.aistarter.prompt.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromptSeedRunner implements ApplicationRunner {

    private final PromptService promptService;

    @Override
    public void run(ApplicationArguments args) {
        seed(PromptFallbacks.KEY_CHAT, PromptType.system, "Default chat system prompt");
        seed(PromptFallbacks.KEY_DATABASE_AGENT, PromptType.system, "Database analyze agent system prompt");
        seed(PromptFallbacks.KEY_DATABASE_AGENT, PromptType.user, "Database analyze agent user template");
        seed(PromptFallbacks.KEY_RAG_CHAT, PromptType.system, "RAG chat system prompt");
        seed(PromptFallbacks.KEY_RAG_CHAT, PromptType.user, "RAG chat user template");
    }

    private void seed(String key, PromptType type, String description) {
        if (promptService.definitionExists(key, type)) {
            return;
        }
        String content = PromptFallbacks.fallback(key, type);
        promptService.createVersion(key, type, content, description);
        promptService.setActiveVersion(key, type, 1);
    }
}
