package com.aistarter.rag.parser;

import java.util.HashMap;
import java.util.Map;

public record ParseResult(String text, Map<String, Object> metadata) {

    public static ParseResult of(String text) {
        return new ParseResult(text, Map.of("parser", "text"));
    }

    public static ParseResult of(String text, Map<String, Object> metadata) {
        return new ParseResult(text, new HashMap<>(metadata));
    }
}
