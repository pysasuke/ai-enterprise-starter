package com.aistarter.workflow.engine;

public record StepTrace(
        String name,
        WorkflowStepStatus status,
        long durationMs,
        String summary) {}
