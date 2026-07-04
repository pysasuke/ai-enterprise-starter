package com.aistarter.web.controller;

import com.aistarter.common.constant.AppConstants;
import com.aistarter.workflow.dto.WorkflowDatabaseAnalyzeRequest;
import com.aistarter.workflow.dto.WorkflowDatabaseAnalyzeResponse;
import com.aistarter.workflow.dto.WorkflowStepTraceDto;
import com.aistarter.workflow.engine.WorkflowRunResult;
import com.aistarter.workflow.engine.WorkflowStepStatus;
import com.aistarter.workflow.engine.StepTrace;
import com.aistarter.workflow.workflow.DatabaseAnalyzeWorkflow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/workflows")
@RequiredArgsConstructor
@Tag(name = "Workflow")
public class WorkflowController {

    private final DatabaseAnalyzeWorkflow databaseAnalyzeWorkflow;

    @PostMapping("/database-analyze")
    @Operation(summary = "Database Analyze Workflow (multi-step)")
    public ResponseEntity<WorkflowDatabaseAnalyzeResponse> databaseAnalyze(
            @Valid @RequestBody WorkflowDatabaseAnalyzeRequest request) {
        WorkflowRunResult result = databaseAnalyzeWorkflow.execute(request);
        WorkflowDatabaseAnalyzeResponse body = databaseAnalyzeWorkflow.toResponse(result);
        if (!result.success()) {
            return ResponseEntity.badRequest().body(body);
        }
        return ResponseEntity.ok(body);
    }
}
