package com.aistarter.mcp.support;

import com.aistarter.mcp.service.ToolExecutor;
import com.aistarter.mcp.tool.Tool;
import com.aistarter.mcp.tool.ToolExecutionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ToolCallbackFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ToolCallbackFactory() {}

    public interface ToolExecutionListener {
        void onToolCall(String toolId, String toolName, Map<String, Object> arguments);

        void onToolResult(String toolId, String toolName, ToolExecutionResult result);
    }

    public static List<ToolCallback> from(List<Tool> tools, ToolExecutor executor, ToolExecutionListener listener) {
        List<ToolCallback> callbacks = new ArrayList<>();
        for (Tool tool : tools) {
            callbacks.add(createCallback(tool, executor, listener));
        }
        return callbacks;
    }

    private static ToolCallback createCallback(Tool tool, ToolExecutor executor, ToolExecutionListener listener) {
        return FunctionToolCallback.builder(tool.getName(), (Map<String, Object> input) -> {
                    String toolId = UUID.randomUUID().toString();
                    listener.onToolCall(toolId, tool.getName(), input);
                    ToolExecutionResult result = executor.execute(tool.getName(), input);
                    listener.onToolResult(toolId, tool.getName(), result);
                    if (result.success()) {
                        return result.result();
                    }
                    return "Error: " + result.error();
                })
                .description(tool.getDescription())
                .inputSchema(toJsonSchema(tool.getParametersSchema()))
                .inputType(Map.class)
                .build();
    }

    private static String toJsonSchema(Map<String, Object> schema) {
        try {
            return OBJECT_MAPPER.writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize tool schema", e);
        }
    }
}
