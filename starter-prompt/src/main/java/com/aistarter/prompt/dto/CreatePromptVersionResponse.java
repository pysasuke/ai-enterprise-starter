package com.aistarter.prompt.dto;

import com.aistarter.prompt.entity.PromptType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePromptVersionResponse {

    private String key;
    private PromptType type;
    private int version;
    private boolean active;
}
