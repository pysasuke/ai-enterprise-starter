package com.aistarter.web.controller;

import com.aistarter.workflow.dto.WorkflowAgentRouteMetadata;
import com.aistarter.workflow.dto.WorkflowAgentRouteRequest;
import com.aistarter.workflow.dto.WorkflowAgentRouteResponse;
import com.aistarter.workflow.dto.WorkflowDatabaseAnalyzeRequest;
import com.aistarter.workflow.dto.WorkflowDatabaseAnalyzeResponse;
import com.aistarter.workflow.dto.WorkflowStepTraceDto;
import com.aistarter.workflow.engine.StepTrace;
import com.aistarter.workflow.engine.WorkflowRunResult;
import com.aistarter.workflow.engine.WorkflowStepStatus;
import com.aistarter.workflow.engine.WorkflowStepStatus;
import com.aistarter.workflow.router.AgentType;
import com.aistarter.workflow.workflow.AgentRouteExecutionResult;
import com.aistarter.workflow.workflow.AgentRouteWorkflow;
import com.aistarter.workflow.workflow.DatabaseAnalyzeWorkflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {WorkflowController.class})
class WorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private DatabaseAnalyzeWorkflow databaseAnalyzeWorkflow;
    @MockBean
    private AgentRouteWorkflow agentRouteWorkflow;

    @Test
    void returnsAnalysisAndStepsOnSuccess() throws Exception {
        WorkflowRunResult runResult = new WorkflowRunResult(
                true,
                "add index on user_id",
                List.of(
                        new StepTrace("load-schema", WorkflowStepStatus.SUCCESS, 10, "loaded"),
                        new StepTrace("analyze", WorkflowStepStatus.SUCCESS, 100, "generated"),
                        new StepTrace("format-output", WorkflowStepStatus.SUCCESS, 1, "formatted")),
                null);
        when(databaseAnalyzeWorkflow.execute(any(WorkflowDatabaseAnalyzeRequest.class))).thenReturn(runResult);
        when(databaseAnalyzeWorkflow.toResponse(runResult)).thenReturn(new WorkflowDatabaseAnalyzeResponse(
                "add index on user_id",
                List.of(
                        new WorkflowStepTraceDto("load-schema", "SUCCESS", 10, "loaded"),
                        new WorkflowStepTraceDto("analyze", "SUCCESS", 100, "generated"),
                        new WorkflowStepTraceDto("format-output", "SUCCESS", 1, "formatted"))));

        mockMvc.perform(post("/api/workflows/database-analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"why slow?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis").value("add index on user_id"))
                .andExpect(jsonPath("$.steps.length()").value(3))
                .andExpect(jsonPath("$.steps[0].name").value("load-schema"));
    }

    @Test
    void returnsBadRequestWithPartialStepsOnFailure() throws Exception {
        WorkflowRunResult runResult = new WorkflowRunResult(
                false,
                null,
                List.of(
                        new StepTrace("load-schema", WorkflowStepStatus.SUCCESS, 10, "loaded"),
                        new StepTrace("analyze", WorkflowStepStatus.FAILED, 50, "llm failed")),
                "llm failed");
        when(databaseAnalyzeWorkflow.execute(any(WorkflowDatabaseAnalyzeRequest.class))).thenReturn(runResult);
        when(databaseAnalyzeWorkflow.toResponse(runResult)).thenReturn(new WorkflowDatabaseAnalyzeResponse(
                null,
                List.of(
                        new WorkflowStepTraceDto("load-schema", "SUCCESS", 10, "loaded"),
                        new WorkflowStepTraceDto("analyze", "FAILED", 50, "llm failed"))));

        mockMvc.perform(post("/api/workflows/database-analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"why slow?\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.analysis").doesNotExist())
                .andExpect(jsonPath("$.steps.length()").value(2))
                .andExpect(jsonPath("$.steps[1].status").value("FAILED"));
    }

    @Test
    void returnsAgentRouteAnswerAndStepsOnSuccess() throws Exception {
        WorkflowRunResult runResult = new WorkflowRunResult(
                true,
                "refund in 7 days",
                List.of(
                        new StepTrace("classify", WorkflowStepStatus.SUCCESS, 5, "RULE:RAG"),
                        new StepTrace("execute-agent", WorkflowStepStatus.SUCCESS, 100, "RAG executed")),
                null);
        AgentRouteExecutionResult executionResult =
                new AgentRouteExecutionResult(runResult, AgentType.RAG, new WorkflowAgentRouteMetadata());
        when(agentRouteWorkflow.execute(any(WorkflowAgentRouteRequest.class))).thenReturn(executionResult);
        when(agentRouteWorkflow.toResponse(executionResult)).thenReturn(new WorkflowAgentRouteResponse(
                "refund in 7 days",
                "RAG",
                List.of(
                        new WorkflowStepTraceDto("classify", "SUCCESS", 5, "RULE:RAG"),
                        new WorkflowStepTraceDto("execute-agent", "SUCCESS", 100, "RAG executed")),
                new WorkflowAgentRouteMetadata()));

        mockMvc.perform(post("/api/workflows/agent-route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"退款政策是什么？\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("refund in 7 days"))
                .andExpect(jsonPath("$.selectedAgent").value("RAG"))
                .andExpect(jsonPath("$.steps.length()").value(2));
    }

    @Test
    void returnsBadRequestWhenAgentRouteExecuteFails() throws Exception {
        WorkflowRunResult runResult = new WorkflowRunResult(
                false,
                null,
                List.of(
                        new StepTrace("classify", WorkflowStepStatus.SUCCESS, 5, "RULE:DATABASE"),
                        new StepTrace("execute-agent", WorkflowStepStatus.FAILED, 50, "agent failed")),
                "agent failed");
        AgentRouteExecutionResult executionResult =
                new AgentRouteExecutionResult(runResult, AgentType.DATABASE, new WorkflowAgentRouteMetadata());
        when(agentRouteWorkflow.execute(any(WorkflowAgentRouteRequest.class))).thenReturn(executionResult);
        when(agentRouteWorkflow.toResponse(executionResult)).thenReturn(new WorkflowAgentRouteResponse(
                null,
                "DATABASE",
                List.of(
                        new WorkflowStepTraceDto("classify", "SUCCESS", 5, "RULE:DATABASE"),
                        new WorkflowStepTraceDto("execute-agent", "FAILED", 50, "agent failed")),
                new WorkflowAgentRouteMetadata()));

        mockMvc.perform(post("/api/workflows/agent-route")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"slow query?\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.steps[1].status").value("FAILED"));
    }
}
