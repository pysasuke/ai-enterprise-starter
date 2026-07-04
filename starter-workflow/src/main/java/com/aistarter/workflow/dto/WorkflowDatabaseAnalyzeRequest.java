package com.aistarter.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkflowDatabaseAnalyzeRequest {

    @NotBlank
    private String question;
}
