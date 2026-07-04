package com.aistarter.workflow.engine;

import java.util.List;

public record WorkflowRunResult(
        boolean success,
        String analysis,
        List<StepTrace> traces,
        String errorMessage) {}
