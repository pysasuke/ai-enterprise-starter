package com.aistarter.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowAgentRouteRequest {

    @NotBlank
    private String question;

    private int topK = 5;

    private String sessionId = "default";
}
