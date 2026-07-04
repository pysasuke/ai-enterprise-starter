package com.aistarter.workflow.steps;

import com.aistarter.agent.dto.DatabaseAgentRequest;
import com.aistarter.agent.dto.DatabaseAgentResponse;
import com.aistarter.agent.service.DatabaseAnalyzeAgent;
import com.aistarter.ai.dto.ChatRequest;
import com.aistarter.ai.dto.ChatResponse;
import com.aistarter.ai.service.ChatService;
import com.aistarter.rag.dto.RagChatRequest;
import com.aistarter.rag.dto.RagChatResponse;
import com.aistarter.rag.dto.RagSource;
import com.aistarter.rag.service.RagChatService;
import com.aistarter.workflow.dto.WorkflowAgentRouteMetadata;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowKeys;
import com.aistarter.workflow.router.AgentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecuteAgentStepTest {

    @Mock
    private ChatService chatService;
    @Mock
    private RagChatService ragChatService;
    @Mock
    private DatabaseAnalyzeAgent databaseAnalyzeAgent;
    @InjectMocks
    private ExecuteAgentStep step;

    @Test
    void executesDatabaseAgent() {
        WorkflowContext context = new WorkflowContext();
        context.put(WorkflowKeys.SELECTED_AGENT, AgentType.DATABASE);
        context.put(WorkflowKeys.QUESTION, "slow query?");
        when(databaseAnalyzeAgent.analyze(any(DatabaseAgentRequest.class)))
                .thenReturn(new DatabaseAgentResponse("add index"));

        step.execute(context);

        assertThat(context.getString(WorkflowKeys.ANSWER)).isEqualTo("add index");
        assertThat(context.getString(WorkflowKeys.ANALYSIS)).isEqualTo("add index");
    }

    @Test
    void executesChatAgent() {
        WorkflowContext context = new WorkflowContext();
        context.put(WorkflowKeys.SELECTED_AGENT, AgentType.CHAT);
        context.put(WorkflowKeys.QUESTION, "hello");
        context.put(WorkflowKeys.SESSION_ID, "demo");
        when(chatService.chat(any(ChatRequest.class), eq("demo")))
                .thenReturn(new ChatResponse("hi there"));

        step.execute(context);

        assertThat(context.getString(WorkflowKeys.ANSWER)).isEqualTo("hi there");
    }

    @Test
    void executesRagAgentWithSources() {
        WorkflowContext context = new WorkflowContext();
        context.put(WorkflowKeys.SELECTED_AGENT, AgentType.RAG);
        context.put(WorkflowKeys.QUESTION, "refund policy?");
        context.put(WorkflowKeys.TOP_K, 3);
        List<RagSource> sources = List.of(new RagSource(1L, "policy.md", "snippet"));
        when(ragChatService.chat(any(RagChatRequest.class)))
                .thenReturn(new RagChatResponse("refund in 7 days", sources));

        step.execute(context);

        assertThat(context.getString(WorkflowKeys.ANSWER)).isEqualTo("refund in 7 days");
        WorkflowAgentRouteMetadata metadata = context.get(WorkflowKeys.METADATA);
        assertThat(metadata.getSources()).hasSize(1);
    }
}
