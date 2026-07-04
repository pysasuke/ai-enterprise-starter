package com.aistarter.workflow.steps;

import com.aistarter.agent.service.DatabaseSchemaService;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadSchemaStepTest {

    @Mock
    private DatabaseSchemaService schemaService;

    @InjectMocks
    private LoadSchemaStep loadSchemaStep;

    @Test
    void loadsSchemaIntoContext() {
        when(schemaService.loadSchemaSummary()).thenReturn("orders table");
        when(schemaService.loadIndexSummary()).thenReturn("idx_user_id");

        WorkflowContext context = new WorkflowContext();
        loadSchemaStep.execute(context);

        assertThat(context.getString(WorkflowKeys.SCHEMA)).isEqualTo("orders table");
        assertThat(context.getString(WorkflowKeys.INDEXES)).isEqualTo("idx_user_id");
        assertThat(loadSchemaStep.name()).isEqualTo("load-schema");
    }
}
