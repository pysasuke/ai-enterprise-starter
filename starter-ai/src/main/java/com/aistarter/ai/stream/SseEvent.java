package com.aistarter.ai.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record SseEvent(String type, Map<String, Object> payload) {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static SseEvent start(String conversationId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "start");
        payload.put("conversationId", conversationId);
        payload.put("timestamp", System.currentTimeMillis());
        return new SseEvent("start", payload);
    }

    public static SseEvent chunk(String content) {
        return new SseEvent("chunk", Map.of("type", "chunk", "content", content));
    }

    public static SseEvent toolCall(String toolId, String toolName, Map<String, Object> arguments) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "tool_call");
        payload.put("toolId", toolId);
        payload.put("toolName", toolName);
        payload.put("arguments", arguments);
        return new SseEvent("tool_call", payload);
    }

    public static SseEvent toolResult(String toolId, String toolName, boolean success, String result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "tool_result");
        payload.put("toolId", toolId);
        payload.put("toolName", toolName);
        payload.put("success", success);
        payload.put("result", result);
        return new SseEvent("tool_result", payload);
    }

    public static SseEvent done(long durationMs, Map<String, Object> metadata) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "done");
        payload.put("durationMs", durationMs);
        if (metadata != null && !metadata.isEmpty()) {
            payload.put("metadata", metadata);
        }
        return new SseEvent("done", payload);
    }

    public static SseEvent error(String code, String message, String toolName) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "error");
        payload.put("code", code);
        payload.put("message", message);
        if (toolName != null) {
            payload.put("toolName", toolName);
        }
        return new SseEvent("error", payload);
    }

    public static String newConversationId() {
        return UUID.randomUUID().toString();
    }

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize SSE event", e);
        }
    }
}
