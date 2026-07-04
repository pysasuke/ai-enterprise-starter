package com.aistarter.workflow.engine;

public interface WorkflowStep {

    String name();

    StepResult execute(WorkflowContext context);
}
