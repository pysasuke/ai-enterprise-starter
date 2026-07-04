package com.aistarter.workflow.dto;

public record WorkflowStepTraceDto(
        String name,
        String status,
        long durationMs,
        String summary) {}
