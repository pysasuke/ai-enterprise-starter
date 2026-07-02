package com.aistarter.prompt.dto;

import com.aistarter.prompt.entity.PromptType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PromptDefinitionResponse {

    private String key;
    private PromptType type;
    private String description;
    private Integer activeVersion;
    private String activeContentPreview;
}
