package com.aistarter.ai.stream;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SseEventTest {

    @Test
    void serializesChunkEvent() {
        String json = SseEvent.chunk("hello").toJson();
        assertTrue(json.contains("\"type\":\"chunk\""));
        assertTrue(json.contains("\"content\":\"hello\""));
    }

    @Test
    void serializesDoneWithMetadata() {
        String json = SseEvent.done(120, Map.of("sources", "[]")).toJson();
        assertTrue(json.contains("\"type\":\"done\""));
        assertTrue(json.contains("\"durationMs\":120"));
    }
}
