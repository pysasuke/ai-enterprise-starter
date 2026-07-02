package com.aistarter.prompt.service;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.prompt.dto.CreatePromptVersionResponse;
import com.aistarter.prompt.dto.PromptDefinitionResponse;
import com.aistarter.prompt.dto.PromptRenderResponse;
import com.aistarter.prompt.dto.PromptVersionResponse;
import com.aistarter.prompt.entity.PromptDefinition;
import com.aistarter.prompt.entity.PromptType;
import com.aistarter.prompt.entity.PromptVersion;
import com.aistarter.prompt.repository.PromptDefinitionRepository;
import com.aistarter.prompt.repository.PromptVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromptService {

    private static final int PREVIEW_MAX_LENGTH = 120;

    private final PromptDefinitionRepository definitionRepository;
    private final PromptVersionRepository versionRepository;
    private final PromptTemplateRenderer templateRenderer;

    public List<PromptDefinitionResponse> listDefinitions() {
        return definitionRepository.findAllByOrderByPromptKeyAscTypeAsc().stream()
                .map(this::toDefinitionResponse)
                .toList();
    }

    public PromptDefinitionResponse getDefinition(String promptKey, PromptType type) {
        PromptDefinition definition = findDefinitionOrThrow(promptKey, type);
        return toDefinitionResponse(definition);
    }

    public List<PromptVersionResponse> listVersions(String promptKey, PromptType type) {
        PromptDefinition definition = findDefinitionOrThrow(promptKey, type);
        return versionRepository.findByDefinitionOrderByVersionDesc(definition).stream()
                .map(version -> toVersionResponse(definition, version))
                .toList();
    }

    @Transactional
    public CreatePromptVersionResponse createVersion(String promptKey, PromptType type, String content, String description) {
        PromptDefinition definition = definitionRepository.findByPromptKeyAndType(promptKey, type)
                .orElseGet(() -> {
                    PromptDefinition created = new PromptDefinition();
                    created.setPromptKey(promptKey);
                    created.setType(type);
                    created.setDescription(description);
                    return definitionRepository.save(created);
                });

        if (description != null && !description.isBlank() && definition.getDescription() == null) {
            definition.setDescription(description);
            definitionRepository.save(definition);
        }

        int nextVersion = versionRepository.findMaxVersion(definition) + 1;
        PromptVersion version = new PromptVersion();
        version.setDefinition(definition);
        version.setVersion(nextVersion);
        version.setContent(content);
        versionRepository.save(version);

        boolean active = definition.getActiveVersion() != null && definition.getActiveVersion() == nextVersion;
        return new CreatePromptVersionResponse(promptKey, type, nextVersion, active);
    }

    @Transactional
    public PromptDefinitionResponse setActiveVersion(String promptKey, PromptType type, int version) {
        PromptDefinition definition = findDefinitionOrThrow(promptKey, type);
        versionRepository.findByDefinitionAndVersion(definition, version)
                .orElseThrow(() -> new BusinessException("Prompt version not found: " + version));
        definition.setActiveVersion(version);
        definitionRepository.save(definition);
        return toDefinitionResponse(definition);
    }

    @Transactional
    public void deleteVersion(String promptKey, PromptType type, int version) {
        PromptDefinition definition = findDefinitionOrThrow(promptKey, type);
        if (definition.getActiveVersion() != null && definition.getActiveVersion() == version) {
            throw new BusinessException(409, "Cannot delete active prompt version");
        }
        PromptVersion promptVersion = versionRepository.findByDefinitionAndVersion(definition, version)
                .orElseThrow(() -> new BusinessException("Prompt version not found: " + version));
        versionRepository.delete(promptVersion);
    }

    public PromptRenderResponse renderForApi(String promptKey, PromptType type, Integer version, Map<String, ?> variables) {
        ResolvedPrompt resolved = resolve(promptKey, type, version, true);
        String rendered = templateRenderer.render(resolved.content(), variables);
        return new PromptRenderResponse(promptKey, type, resolved.version(), rendered);
    }

    public String render(String promptKey, PromptType type, Map<String, ?> variables) {
        ResolvedPrompt resolved = resolve(promptKey, type, null, true);
        return templateRenderer.render(resolved.content(), variables);
    }

    public String render(String promptKey, PromptType type, int version, Map<String, ?> variables) {
        ResolvedPrompt resolved = resolve(promptKey, type, version, true);
        return templateRenderer.render(resolved.content(), variables);
    }

    public Optional<String> renderOptional(String promptKey, PromptType type, Map<String, ?> variables) {
        Optional<ResolvedPrompt> resolved = resolveOptional(promptKey, type, null);
        return resolved.map(value -> templateRenderer.render(value.content(), variables));
    }

    public boolean definitionExists(String promptKey, PromptType type) {
        return definitionRepository.findByPromptKeyAndType(promptKey, type).isPresent();
    }

    private ResolvedPrompt resolve(String promptKey, PromptType type, Integer version, boolean useFallback) {
        if (version != null) {
            return resolveOptional(promptKey, type, version)
                    .orElseThrow(() -> new BusinessException("Prompt version not found: " + version));
        }
        return resolveOptional(promptKey, type, null)
                .orElseGet(() -> {
                    if (!useFallback) {
                        throw new BusinessException("No active prompt for " + promptKey + "/" + type);
                    }
                    return new ResolvedPrompt(0, PromptFallbacks.fallback(promptKey, type));
                });
    }

    private Optional<ResolvedPrompt> resolveOptional(String promptKey, PromptType type, Integer version) {
        Optional<PromptDefinition> definitionOpt = definitionRepository.findByPromptKeyAndType(promptKey, type);
        if (definitionOpt.isEmpty()) {
            return Optional.empty();
        }
        PromptDefinition definition = definitionOpt.get();
        int targetVersion = version != null ? version : definition.getActiveVersion() != null ? definition.getActiveVersion() : -1;
        if (targetVersion <= 0) {
            return Optional.empty();
        }
        return versionRepository.findByDefinitionAndVersion(definition, targetVersion)
                .map(v -> new ResolvedPrompt(v.getVersion(), v.getContent()));
    }

    private PromptDefinition findDefinitionOrThrow(String promptKey, PromptType type) {
        return definitionRepository.findByPromptKeyAndType(promptKey, type)
                .orElseThrow(() -> new BusinessException(404, "Prompt not found: " + promptKey + "/" + type));
    }

    private PromptDefinitionResponse toDefinitionResponse(PromptDefinition definition) {
        String preview = null;
        if (definition.getActiveVersion() != null) {
            preview = versionRepository.findByDefinitionAndVersion(definition, definition.getActiveVersion())
                    .map(PromptVersion::getContent)
                    .map(this::preview)
                    .orElse(null);
        }
        return new PromptDefinitionResponse(
                definition.getPromptKey(),
                definition.getType(),
                definition.getDescription(),
                definition.getActiveVersion(),
                preview);
    }

    private PromptVersionResponse toVersionResponse(PromptDefinition definition, PromptVersion version) {
        boolean active = definition.getActiveVersion() != null
                && definition.getActiveVersion() == version.getVersion();
        return new PromptVersionResponse(
                definition.getPromptKey(),
                definition.getType(),
                version.getVersion(),
                version.getContent(),
                active,
                version.getCreatedAt());
    }

    private String preview(String content) {
        if (content == null) {
            return null;
        }
        if (content.length() <= PREVIEW_MAX_LENGTH) {
            return content;
        }
        return content.substring(0, PREVIEW_MAX_LENGTH) + "...";
    }

    private record ResolvedPrompt(int version, String content) {
    }
}
