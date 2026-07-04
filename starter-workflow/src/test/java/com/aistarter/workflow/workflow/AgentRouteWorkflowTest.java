package com.aistarter.workflow.workflow;

import com.aistarter.workflow.dto.WorkflowAgentRouteRequest;
import com.aistarter.workflow.engine.StepTrace;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowRunResult;
import com.aistarter.workflow.engine.WorkflowRunner;
import com.aistarter.workflow.engine.WorkflowStepStatus;
import com.aistarter.workflow.router.AgentType;
import com.aistarter.workflow.steps.ClassifyAgentStep;
import com.aistarter.workflow.steps.ExecuteAgentStep;
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
class AgentRouteWorkflowTest {

    @Mock
    private WorkflowRunner runner;
    @Mock
    private ClassifyAgentStep classifyAgentStep;
    @Mock
    private ExecuteAgentStep executeAgentStep;
    @InjectMocks
    private AgentRouteWorkflow workflow;

    @Test
    void executeReturnsSelectedAgentAndAnswer() {
        WorkflowRunResult runResult = new WorkflowRunResult(
                true,
                "refund in 7 days",
                List.of(
                        new StepTrace("classify", WorkflowStepStatus.SUCCESS, 5, "RULE:RAG"),
                        new StepTrace("execute-agent", WorkflowStepStatus.SUCCESS, 100, "RAG executed")),
                null);
        when(runner.run(eq(List.of(classifyAgentStep, executeAgentStep)), any(WorkflowContext.class)))
                .thenAnswer(invocation -> {
                    WorkflowContext context = invocation.getArgument(1);
                    context.put(com.aistarter.workflow.engine.WorkflowKeys.SELECTED_AGENT, AgentType.RAG);
                    return runResult;
                });

        WorkflowAgentRouteRequest request = new WorkflowAgentRouteRequest();
        request.setQuestion("退款政策是什么？");

        AgentRouteExecutionResult result = workflow.execute(request);

        assertThat(result.selectedAgent()).isEqualTo(AgentType.RAG);
        assertThat(result.runResult().analysis()).isEqualTo("refund in 7 days");
    }

    @Test
    void toResponseMapsFields() {
        WorkflowRunResult runResult = new WorkflowRunResult(
                true,
                "hello",
                List.of(new StepTrace("classify", WorkflowStepStatus.SUCCESS, 1, "LLM:CHAT")),
                null);
        AgentRouteExecutionResult executionResult =
                new AgentRouteExecutionResult(runResult, AgentType.CHAT, new com.aistarter.workflow.dto.WorkflowAgentRouteMetadata());

        var response = workflow.toResponse(executionResult);

        assertThat(response.answer()).isEqualTo("hello");
        assertThat(response.selectedAgent()).isEqualTo("CHAT");
        assertThat(response.steps()).hasSize(1);
    }
}
