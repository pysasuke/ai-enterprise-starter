package com.aistarter.workflow.steps;

import com.aistarter.agent.dto.DatabaseAgentRequest;
import com.aistarter.agent.dto.DatabaseAgentResponse;
import com.aistarter.agent.service.DatabaseAnalyzeAgent;
import com.aistarter.ai.dto.ChatRequest;
import com.aistarter.ai.dto.ChatResponse;
import com.aistarter.ai.service.ChatService;
import com.aistarter.rag.dto.RagChatRequest;
import com.aistarter.rag.dto.RagChatResponse;
import com.aistarter.rag.service.RagChatService;
import com.aistarter.workflow.dto.WorkflowAgentRouteMetadata;
import com.aistarter.workflow.engine.StepResult;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowKeys;
import com.aistarter.workflow.engine.WorkflowStep;
import com.aistarter.workflow.router.AgentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExecuteAgentStep implements WorkflowStep {

    private final ChatService chatService;
    private final RagChatService ragChatService;
    private final DatabaseAnalyzeAgent databaseAnalyzeAgent;

    @Override
    public String name() {
        return "execute-agent";
    }

    @Override
    public StepResult execute(WorkflowContext context) {
        AgentType agent = context.get(WorkflowKeys.SELECTED_AGENT);
        String question = context.getString(WorkflowKeys.QUESTION);
        String answer;
        WorkflowAgentRouteMetadata metadata = new WorkflowAgentRouteMetadata();

        switch (agent) {
            case CHAT -> {
                String sessionId = context.getString(WorkflowKeys.SESSION_ID);
                if (sessionId == null || sessionId.isBlank()) {
                    sessionId = "default";
                }
                ChatRequest chatRequest = new ChatRequest();
                chatRequest.setMessage(question);
                ChatResponse chatResponse = chatService.chat(chatRequest, sessionId);
                answer = chatResponse.getContent();
            }
            case RAG -> {
                int topK = context.get(WorkflowKeys.TOP_K) != null
                        ? (Integer) context.get(WorkflowKeys.TOP_K) : 5;
                RagChatRequest ragRequest = new RagChatRequest();
                ragRequest.setQuestion(question);
                ragRequest.setTopK(topK);
                RagChatResponse ragResponse = ragChatService.chat(ragRequest);
                answer = ragResponse.getAnswer();
                metadata.setSources(ragResponse.getSources());
            }
            case DATABASE -> {
                DatabaseAgentRequest dbRequest = new DatabaseAgentRequest();
                dbRequest.setQuestion(question);
                DatabaseAgentResponse dbResponse = databaseAnalyzeAgent.analyze(dbRequest);
                answer = dbResponse.getAnalysis();
            }
            default -> throw new IllegalStateException("Unknown agent: " + agent);
        }

        context.put(WorkflowKeys.ANSWER, answer);
        context.put(WorkflowKeys.ANALYSIS, answer);
        context.put(WorkflowKeys.METADATA, metadata);
        return new StepResult(agent.name() + " executed");
    }
}
