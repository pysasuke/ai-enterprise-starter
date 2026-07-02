package com.aistarter.prompt.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PromptTemplateRendererTest {

    private final PromptTemplateRenderer renderer = new PromptTemplateRenderer();

    @Test
    void replacesVariables() {
        String out = renderer.render("Hello {{name}}, id={{id}}", Map.of("name", "Alice", "id", "1"));
        assertThat(out).isEqualTo("Hello Alice, id=1");
    }

    @Test
    void leavesMissingVariablesUntouched() {
        String out = renderer.render("Q: {{question}}", Map.of());
        assertThat(out).isEqualTo("Q: {{question}}");
    }
}
