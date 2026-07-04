package com.aistarter.mcp.tool;

public record ToolExecutionResult(boolean success, String result, String error) {

    public static ToolExecutionResult ok(String result) {
        return new ToolExecutionResult(true, result, null);
    }

    public static ToolExecutionResult fail(String error) {
        return new ToolExecutionResult(false, null, error);
    }
}
