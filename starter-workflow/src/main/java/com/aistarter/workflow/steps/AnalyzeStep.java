package com.aistarter.workflow.steps;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.prompt.entity.PromptType;
import com.aistarter.prompt.service.PromptFallbacks;
import com.aistarter.prompt.service.PromptService;
import com.aistarter.workflow.engine.StepResult;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowKeys;
import com.aistarter.workflow.engine.WorkflowStep;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AnalyzeStep implements WorkflowStep {

    private final ChatClient.Builder chatClientBuilder;
    private final PromptService promptService;

    @Override
    public String name() {
        return "analyze";
    }

    @Override
    public StepResult execute(WorkflowContext context) {
        String question = context.getString(WorkflowKeys.QUESTION);
        String schema = context.getString(WorkflowKeys.SCHEMA);
        String indexes = context.getString(WorkflowKeys.INDEXES);

        String systemPrompt = promptService.render(PromptFallbacks.KEY_DATABASE_AGENT, PromptType.system, Map.of());
        String userPrompt = promptService.render(PromptFallbacks.KEY_DATABASE_AGENT, PromptType.user, Map.of(
                "question", question,
                "schema", schema,
                "indexes", indexes));

        String raw = chatClientBuilder.build()
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
        if (raw == null || raw.isBlank()) {
            throw new BusinessException("LLM returned empty analysis");
        }
        context.put(WorkflowKeys.RAW_ANALYSIS, raw);
        return new StepResult("analysis generated");
    }
}
