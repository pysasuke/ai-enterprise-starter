package com.aistarter.workflow.dto;

import java.util.List;

public record WorkflowAgentRouteResponse(
        String answer,
        String selectedAgent,
        List<WorkflowStepTraceDto> steps,
        WorkflowAgentRouteMetadata metadata) {}
