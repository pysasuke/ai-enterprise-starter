package com.aistarter.workflow.dto;

import java.util.List;

public record WorkflowDatabaseAnalyzeResponse(
        String analysis,
        List<WorkflowStepTraceDto> steps) {}
