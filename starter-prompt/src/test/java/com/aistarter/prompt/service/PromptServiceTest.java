package com.aistarter.prompt.service;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.prompt.entity.PromptDefinition;
import com.aistarter.prompt.entity.PromptType;
import com.aistarter.prompt.entity.PromptVersion;
import com.aistarter.prompt.repository.PromptDefinitionRepository;
import com.aistarter.prompt.repository.PromptVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest {

    @Mock
    private PromptDefinitionRepository definitionRepository;
    @Mock
    private PromptVersionRepository versionRepository;
    @Mock
    private PromptTemplateRenderer templateRenderer;

    @InjectMocks
    private PromptService promptService;

    @Test
    void createVersionIncrementsVersionNumber() {
        PromptDefinition definition = definition("database.agent", PromptType.user);
        definition.setId(1L);
        when(definitionRepository.findByPromptKeyAndType("database.agent", PromptType.user))
                .thenReturn(Optional.of(definition));
        when(versionRepository.findMaxVersion(definition)).thenReturn(1);
        when(versionRepository.save(any(PromptVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = promptService.createVersion("database.agent", PromptType.user, "v2 content", null);

        assertThat(response.getVersion()).isEqualTo(2);
        assertThat(response.isActive()).isFalse();
    }

    @Test
    void setActiveVersionUpdatesDefinition() {
        PromptDefinition definition = definition("database.agent", PromptType.user);
        PromptVersion version = version(definition, 2, "content");
        when(definitionRepository.findByPromptKeyAndType("database.agent", PromptType.user))
                .thenReturn(Optional.of(definition));
        when(versionRepository.findByDefinitionAndVersion(definition, 2)).thenReturn(Optional.of(version));
        when(definitionRepository.save(definition)).thenReturn(definition);

        var response = promptService.setActiveVersion("database.agent", PromptType.user, 2);

        assertThat(response.getActiveVersion()).isEqualTo(2);
        verify(definitionRepository).save(definition);
    }

    @Test
    void renderUsesActiveVersionWhenVersionOmitted() {
        PromptDefinition definition = definition("database.agent", PromptType.user);
        definition.setActiveVersion(1);
        PromptVersion version = version(definition, 1, "Hello {{name}}");
        when(definitionRepository.findByPromptKeyAndType("database.agent", PromptType.user))
                .thenReturn(Optional.of(definition));
        when(versionRepository.findByDefinitionAndVersion(definition, 1)).thenReturn(Optional.of(version));
        when(templateRenderer.render("Hello {{name}}", Map.of("name", "Bob"))).thenReturn("Hello Bob");

        String rendered = promptService.render("database.agent", PromptType.user, Map.of("name", "Bob"));

        assertThat(rendered).isEqualTo("Hello Bob");
    }

    @Test
    void deleteActiveVersionThrows() {
        PromptDefinition definition = definition("database.agent", PromptType.user);
        definition.setActiveVersion(1);
        when(definitionRepository.findByPromptKeyAndType("database.agent", PromptType.user))
                .thenReturn(Optional.of(definition));

        assertThatThrownBy(() -> promptService.deleteVersion("database.agent", PromptType.user, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("active");
    }

    @Test
    void renderFallsBackWhenNoActive() {
        when(definitionRepository.findByPromptKeyAndType("database.agent", PromptType.system))
                .thenReturn(Optional.empty());
        when(templateRenderer.render(PromptFallbacks.fallback("database.agent", PromptType.system), Map.of()))
                .thenReturn("fallback");

        String rendered = promptService.render("database.agent", PromptType.system, Map.of());

        assertThat(rendered).isEqualTo("fallback");
    }

    private PromptDefinition definition(String key, PromptType type) {
        PromptDefinition definition = new PromptDefinition();
        definition.setPromptKey(key);
        definition.setType(type);
        return definition;
    }

    private PromptVersion version(PromptDefinition definition, int versionNumber, String content) {
        PromptVersion version = new PromptVersion();
        version.setDefinition(definition);
        version.setVersion(versionNumber);
        version.setContent(content);
        return version;
    }
}
