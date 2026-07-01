package com.aistarter.rag.parser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class TxtMarkdownParserTest {

    private final TxtMarkdownParser parser = new TxtMarkdownParser();

    @Test
    void supportsTxtAndMarkdown() {
        assertThat(parser.supports("text/plain", "notes.txt")).isTrue();
        assertThat(parser.supports("text/markdown", "readme.md")).isTrue();
        assertThat(parser.supports("application/pdf", "notes.txt")).isTrue();
        assertThat(parser.supports("application/pdf", "file.pdf")).isFalse();
    }

    @Test
    void parsesUtf8Text() throws Exception {
        String text = parser.parse(new ByteArrayInputStream("退款政策：7天无理由".getBytes(StandardCharsets.UTF_8)));
        assertThat(text).contains("退款政策");
    }
}
