package com.aistarter.prompt.dto;

import com.aistarter.prompt.entity.PromptType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PromptVersionResponse {

    private String key;
    private PromptType type;
    private int version;
    private String content;
    private boolean active;
    private LocalDateTime createdAt;
}
