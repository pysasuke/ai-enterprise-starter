package com.aistarter.workflow.workflow;

import com.aistarter.workflow.dto.WorkflowDatabaseAnalyzeRequest;
import com.aistarter.workflow.dto.WorkflowDatabaseAnalyzeResponse;
import com.aistarter.workflow.dto.WorkflowStepTraceDto;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowKeys;
import com.aistarter.workflow.engine.WorkflowRunResult;
import com.aistarter.workflow.engine.WorkflowRunner;
import com.aistarter.workflow.steps.AnalyzeStep;
import com.aistarter.workflow.steps.FormatOutputStep;
import com.aistarter.workflow.steps.LoadSchemaStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseAnalyzeWorkflow {

    private final WorkflowRunner runner;
    private final LoadSchemaStep loadSchemaStep;
    private final AnalyzeStep analyzeStep;
    private final FormatOutputStep formatOutputStep;

    public WorkflowRunResult execute(WorkflowDatabaseAnalyzeRequest request) {
        WorkflowContext context = new WorkflowContext();
        context.put(WorkflowKeys.QUESTION, request.getQuestion());
        return runner.run(List.of(loadSchemaStep, analyzeStep, formatOutputStep), context);
    }

    public WorkflowDatabaseAnalyzeResponse toResponse(WorkflowRunResult result) {
        List<WorkflowStepTraceDto> steps = result.traces().stream()
                .map(trace -> new WorkflowStepTraceDto(
                        trace.name(),
                        trace.status().name(),
                        trace.durationMs(),
                        trace.summary()))
                .toList();
        return new WorkflowDatabaseAnalyzeResponse(result.analysis(), steps);
    }
}
