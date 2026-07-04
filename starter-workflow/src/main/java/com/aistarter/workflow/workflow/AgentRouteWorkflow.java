package com.aistarter.workflow.workflow;

import com.aistarter.workflow.dto.WorkflowAgentRouteMetadata;
import com.aistarter.workflow.dto.WorkflowAgentRouteRequest;
import com.aistarter.workflow.dto.WorkflowAgentRouteResponse;
import com.aistarter.workflow.dto.WorkflowStepTraceDto;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowKeys;
import com.aistarter.workflow.engine.WorkflowRunResult;
import com.aistarter.workflow.engine.WorkflowRunner;
import com.aistarter.workflow.router.AgentType;
import com.aistarter.workflow.steps.ClassifyAgentStep;
import com.aistarter.workflow.steps.ExecuteAgentStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentRouteWorkflow {

    private final WorkflowRunner runner;
    private final ClassifyAgentStep classifyAgentStep;
    private final ExecuteAgentStep executeAgentStep;

    public AgentRouteExecutionResult execute(WorkflowAgentRouteRequest request) {
        WorkflowContext context = new WorkflowContext();
        context.put(WorkflowKeys.QUESTION, request.getQuestion());
        context.put(WorkflowKeys.TOP_K, request.getTopK());
        context.put(WorkflowKeys.SESSION_ID, request.getSessionId());
        WorkflowRunResult runResult = runner.run(List.of(classifyAgentStep, executeAgentStep), context);
        AgentType selectedAgent = context.get(WorkflowKeys.SELECTED_AGENT);
        WorkflowAgentRouteMetadata metadata = context.get(WorkflowKeys.METADATA);
        if (metadata == null) {
            metadata = new WorkflowAgentRouteMetadata();
        }
        return new AgentRouteExecutionResult(runResult, selectedAgent, metadata);
    }

    public WorkflowAgentRouteResponse toResponse(AgentRouteExecutionResult result) {
        List<WorkflowStepTraceDto> steps = result.runResult().traces().stream()
                .map(trace -> new WorkflowStepTraceDto(
                        trace.name(),
                        trace.status().name(),
                        trace.durationMs(),
                        trace.summary()))
                .toList();
        String selectedAgent = result.selectedAgent() != null ? result.selectedAgent().name() : null;
        return new WorkflowAgentRouteResponse(
                result.runResult().analysis(),
                selectedAgent,
                steps,
                result.metadata());
    }
}
