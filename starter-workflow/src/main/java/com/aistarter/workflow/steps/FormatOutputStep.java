package com.aistarter.workflow.steps;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.workflow.engine.StepResult;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowKeys;
import com.aistarter.workflow.engine.WorkflowStep;
import org.springframework.stereotype.Component;

@Component
public class FormatOutputStep implements WorkflowStep {

    @Override
    public String name() {
        return "format-output";
    }

    @Override
    public StepResult execute(WorkflowContext context) {
        String raw = context.getString(WorkflowKeys.RAW_ANALYSIS);
        if (raw == null || raw.isBlank()) {
            throw new BusinessException("No analysis to format");
        }
        context.put(WorkflowKeys.ANALYSIS, raw.trim());
        return new StepResult("output formatted");
    }
}
