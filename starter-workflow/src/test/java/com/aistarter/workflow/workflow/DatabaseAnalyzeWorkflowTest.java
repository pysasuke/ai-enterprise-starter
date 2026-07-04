package com.aistarter.workflow.workflow;

import com.aistarter.workflow.dto.WorkflowDatabaseAnalyzeRequest;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowRunResult;
import com.aistarter.workflow.engine.WorkflowRunner;
import com.aistarter.workflow.steps.AnalyzeStep;
import com.aistarter.workflow.steps.FormatOutputStep;
import com.aistarter.workflow.steps.LoadSchemaStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseAnalyzeWorkflowTest {

    @Mock
    private WorkflowRunner runner;
    @Mock
    private LoadSchemaStep loadSchemaStep;
    @Mock
    private AnalyzeStep analyzeStep;
    @Mock
    private FormatOutputStep formatOutputStep;
    @InjectMocks
    private DatabaseAnalyzeWorkflow workflow;

    @Test
    void executeReturnsThreeStepTraces() {
        WorkflowRunResult runResult = new WorkflowRunResult(
                true,
                "final analysis",
                List.of(),
                null);
        when(runner.run(any(), any(WorkflowContext.class))).thenReturn(runResult);

        WorkflowDatabaseAnalyzeRequest request = new WorkflowDatabaseAnalyzeRequest();
        request.setQuestion("slow query?");

        WorkflowRunResult result = workflow.execute(request);

        assertThat(result.success()).isTrue();
        assertThat(result.analysis()).isEqualTo("final analysis");
    }

    @Test
    void toResponseMapsTraces() {
        WorkflowRunResult runResult = new WorkflowRunResult(
                true,
                "analysis text",
                List.of(new com.aistarter.workflow.engine.StepTrace(
                        "load-schema",
                        com.aistarter.workflow.engine.WorkflowStepStatus.SUCCESS,
                        5,
                        "loaded")),
                null);

        var response = workflow.toResponse(runResult);

        assertThat(response.analysis()).isEqualTo("analysis text");
        assertThat(response.steps()).hasSize(1);
        assertThat(response.steps().get(0).name()).isEqualTo("load-schema");
    }
}
