package com.aistarter.workflow.steps;

import com.aistarter.agent.service.DatabaseSchemaService;
import com.aistarter.workflow.engine.StepResult;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowKeys;
import com.aistarter.workflow.engine.WorkflowStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoadSchemaStep implements WorkflowStep {

    private final DatabaseSchemaService schemaService;

    @Override
    public String name() {
        return "load-schema";
    }

    @Override
    public StepResult execute(WorkflowContext context) {
        String schema = schemaService.loadSchemaSummary();
        String indexes = schemaService.loadIndexSummary();
        context.put(WorkflowKeys.SCHEMA, schema);
        context.put(WorkflowKeys.INDEXES, indexes);
        return new StepResult("schema and indexes loaded");
    }
}
