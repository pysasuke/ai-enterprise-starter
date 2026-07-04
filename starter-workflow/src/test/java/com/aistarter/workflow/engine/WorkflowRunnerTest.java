package com.aistarter.workflow.engine;

import com.aistarter.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowRunnerTest {

    @Test
    void runsAllStepsInOrder() {
        WorkflowRunner runner = new WorkflowRunner();
        WorkflowContext context = new WorkflowContext();

        WorkflowRunResult result = runner.run(List.of(
                step("load-schema", ctx -> {
                    ctx.put("schema", "orders");
                    return new StepResult("schema loaded");
                }),
                step("analyze", ctx -> {
                    ctx.put("analysis", "add index");
                    return new StepResult("done");
                })), context);

        assertThat(result.success()).isTrue();
        assertThat(result.analysis()).isEqualTo("add index");
        assertThat(result.traces()).hasSize(2);
        assertThat(result.traces().get(0).name()).isEqualTo("load-schema");
        assertThat(result.traces().get(0).status()).isEqualTo(WorkflowStepStatus.SUCCESS);
        assertThat(result.traces().get(1).name()).isEqualTo("analyze");
    }

    @Test
    void stopsOnFailureAndKeepsCompletedTraces() {
        WorkflowRunner runner = new WorkflowRunner();
        WorkflowContext context = new WorkflowContext();

        WorkflowRunResult result = runner.run(List.of(
                step("step-a", ctx -> new StepResult("ok")),
                step("step-b", ctx -> {
                    throw new BusinessException("llm failed");
                })), context);

        assertThat(result.success()).isFalse();
        assertThat(result.traces()).hasSize(2);
        assertThat(result.traces().get(0).status()).isEqualTo(WorkflowStepStatus.SUCCESS);
        assertThat(result.traces().get(1).status()).isEqualTo(WorkflowStepStatus.FAILED);
        assertThat(result.traces().get(1).summary()).isEqualTo("llm failed");
    }

    private static WorkflowStep step(String name, StepExecutor executor) {
        return new WorkflowStep() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public StepResult execute(WorkflowContext context) {
                return executor.execute(context);
            }
        };
    }

    @FunctionalInterface
    private interface StepExecutor {
        StepResult execute(WorkflowContext context);
    }
}
