package com.aistarter.mcp.service;

import com.aistarter.mcp.tool.ToolExecutionResult;
import com.aistarter.mcp.tools.CalculateAreaTool;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolExecutorTest {

    @Test
    void executesRegisteredTool() {
        ToolRegistryService registry = new ToolRegistryService(List.of(new CalculateAreaTool()));
        ToolExecutor executor = new ToolExecutor(registry);

        ToolExecutionResult result = executor.execute(
                "calculateArea", Map.of("shape", "rectangle", "width", 2, "height", 5));

        assertTrue(result.success());
        assertEquals("10.00", result.result());
    }
}
