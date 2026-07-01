package com.aistarter.rag.parser;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class TxtMarkdownParser implements DocumentParser {

    @Override
    public boolean supports(String contentType, String filename) {
        if (contentType != null) {
            if (contentType.startsWith("text/plain") || contentType.startsWith("text/markdown")) {
                return true;
            }
        }
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".txt") || lower.endsWith(".md");
    }

    @Override
    public String parse(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
    }
}
