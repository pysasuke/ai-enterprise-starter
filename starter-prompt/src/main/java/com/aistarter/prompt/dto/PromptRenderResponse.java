package com.aistarter.prompt.dto;

import com.aistarter.prompt.entity.PromptType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PromptRenderResponse {

    private String key;
    private PromptType type;
    private int version;
    private String rendered;
}
