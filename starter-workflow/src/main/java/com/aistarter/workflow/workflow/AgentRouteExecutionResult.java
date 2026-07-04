package com.aistarter.workflow.workflow;

import com.aistarter.workflow.dto.WorkflowAgentRouteMetadata;
import com.aistarter.workflow.engine.WorkflowRunResult;
import com.aistarter.workflow.router.AgentType;

public record AgentRouteExecutionResult(
        WorkflowRunResult runResult,
        AgentType selectedAgent,
        WorkflowAgentRouteMetadata metadata) {}
