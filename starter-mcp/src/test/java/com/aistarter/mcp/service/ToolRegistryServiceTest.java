package com.aistarter.mcp.service;

import com.aistarter.mcp.dto.ToolInfo;
import com.aistarter.mcp.tools.CalculateAreaTool;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolRegistryServiceTest {

    @Test
    void listToolsReturnsMetadataForAllRegisteredTools() {
        ToolRegistryService service = new ToolRegistryService(List.of(new CalculateAreaTool()));
        List<ToolInfo> tools = service.listTools();
        assertEquals(1, tools.size());
        assertEquals("calculateArea", tools.get(0).name());
        assertTrue(tools.get(0).readOnly());
    }
}
