package com.aistarter.workflow.engine;

import com.aistarter.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WorkflowRunner {

    public WorkflowRunResult run(List<WorkflowStep> steps, WorkflowContext context) {
        List<StepTrace> traces = new ArrayList<>();
        for (WorkflowStep step : steps) {
            long start = System.currentTimeMillis();
            try {
                StepResult result = step.execute(context);
                long duration = System.currentTimeMillis() - start;
                traces.add(new StepTrace(
                        step.name(), WorkflowStepStatus.SUCCESS, duration, result.summary()));
            } catch (Exception ex) {
                long duration = System.currentTimeMillis() - start;
                String message = ex instanceof BusinessException businessException
                        ? businessException.getMessage()
                        : ex.getMessage();
                traces.add(new StepTrace(step.name(), WorkflowStepStatus.FAILED, duration, message));
                return new WorkflowRunResult(false, null, traces, message);
            }
        }
        return new WorkflowRunResult(true, context.getString("analysis"), traces, null);
    }
}
