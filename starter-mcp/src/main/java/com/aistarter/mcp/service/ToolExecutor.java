package com.aistarter.mcp.service;

import com.aistarter.mcp.tool.ToolExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ToolExecutor {

    private final ToolRegistryService toolRegistryService;

    public ToolExecutionResult execute(String toolName, Map<String, Object> arguments) {
        try {
            return toolRegistryService.findByName(toolName).execute(arguments);
        } catch (Exception e) {
            return ToolExecutionResult.fail(e.getMessage());
        }
    }
}
