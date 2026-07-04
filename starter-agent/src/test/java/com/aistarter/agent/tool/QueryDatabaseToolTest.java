package com.aistarter.agent.tool;

import com.aistarter.agent.dto.DatabaseAgentRequest;
import com.aistarter.agent.dto.DatabaseAgentResponse;
import com.aistarter.agent.service.DatabaseAnalyzeAgent;
import com.aistarter.mcp.tool.ToolExecutionResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QueryDatabaseToolTest {

    @Test
    void delegatesToDatabaseAnalyzeAgent() {
        DatabaseAnalyzeAgent agent = mock(DatabaseAnalyzeAgent.class);
        when(agent.analyze(any())).thenReturn(new DatabaseAgentResponse("add index on user_id"));
        QueryDatabaseTool tool = new QueryDatabaseTool(agent);

        ToolExecutionResult result = tool.execute(Map.of("question", "why slow?"));

        assertTrue(result.success());
        assertEquals("add index on user_id", result.result());
        verify(agent).analyze(any(DatabaseAgentRequest.class));
    }
}
