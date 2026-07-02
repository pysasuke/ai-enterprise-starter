package com.aistarter.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePromptVersionRequest {

    @NotBlank
    private String content;

    private String description;
}
