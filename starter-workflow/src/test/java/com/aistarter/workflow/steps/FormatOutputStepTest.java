package com.aistarter.workflow.steps;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowKeys;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormatOutputStepTest {

    private final FormatOutputStep formatOutputStep = new FormatOutputStep();

    @Test
    void formatsAnalysis() {
        WorkflowContext context = new WorkflowContext();
        context.put(WorkflowKeys.RAW_ANALYSIS, "  trimmed analysis  ");

        formatOutputStep.execute(context);

        assertThat(context.getString(WorkflowKeys.ANALYSIS)).isEqualTo("trimmed analysis");
    }

    @Test
    void rejectsMissingRawAnalysis() {
        assertThatThrownBy(() -> formatOutputStep.execute(new WorkflowContext()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No analysis");
    }
}
