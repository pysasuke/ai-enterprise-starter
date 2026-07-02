package com.aistarter.prompt.dto;

import com.aistarter.prompt.entity.PromptType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class PromptRenderRequest {

    @NotNull
    private String key;

    @NotNull
    private PromptType type;

    private Integer version;

    private Map<String, ?> variables;
}
