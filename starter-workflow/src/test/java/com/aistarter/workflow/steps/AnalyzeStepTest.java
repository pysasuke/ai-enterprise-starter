package com.aistarter.workflow.steps;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.prompt.entity.PromptType;
import com.aistarter.prompt.service.PromptFallbacks;
import com.aistarter.prompt.service.PromptService;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyzeStepTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private PromptService promptService;
    @InjectMocks
    private AnalyzeStep analyzeStep;

    @Test
    void generatesRawAnalysis() {
        when(promptService.render(eq(PromptFallbacks.KEY_DATABASE_AGENT), eq(PromptType.system), any()))
                .thenReturn("system");
        when(promptService.render(eq(PromptFallbacks.KEY_DATABASE_AGENT), eq(PromptType.user), any(Map.class)))
                .thenReturn("user prompt");

        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("  add index  ");

        WorkflowContext context = new WorkflowContext();
        context.put(WorkflowKeys.QUESTION, "slow?");
        context.put(WorkflowKeys.SCHEMA, "schema");
        context.put(WorkflowKeys.INDEXES, "indexes");

        analyzeStep.execute(context);

        assertThat(context.getString(WorkflowKeys.RAW_ANALYSIS)).isEqualTo("  add index  ");
    }

    @Test
    void rejectsEmptyLlmOutput() {
        when(promptService.render(eq(PromptFallbacks.KEY_DATABASE_AGENT), eq(PromptType.system), any()))
                .thenReturn("system");
        when(promptService.render(eq(PromptFallbacks.KEY_DATABASE_AGENT), eq(PromptType.user), any(Map.class)))
                .thenReturn("user prompt");

        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("  ");

        WorkflowContext context = new WorkflowContext();
        context.put(WorkflowKeys.QUESTION, "slow?");
        context.put(WorkflowKeys.SCHEMA, "schema");
        context.put(WorkflowKeys.INDEXES, "indexes");

        assertThatThrownBy(() -> analyzeStep.execute(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("empty analysis");
    }
}
