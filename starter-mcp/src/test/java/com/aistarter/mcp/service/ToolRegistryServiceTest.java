package com.aistarter.mcp.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolRegistryServiceTest {

    private final ToolRegistryService service = new ToolRegistryService();

    @Test
    void listToolsShouldContainBuiltInTools() {
        List<String> tools = service.listTools();
        assertTrue(tools.contains("database"));
        assertTrue(tools.contains("filesystem"));
        assertEquals(2, tools.size());
    }
}
