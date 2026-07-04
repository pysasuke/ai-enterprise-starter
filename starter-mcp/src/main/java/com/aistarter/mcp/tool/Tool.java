package com.aistarter.mcp.tool;

import java.util.Map;

public interface Tool {

    String getName();

    String getDescription();

    Map<String, Object> getParametersSchema();

    ToolExecutionResult execute(Map<String, Object> arguments);

    boolean isReadOnly();
}
