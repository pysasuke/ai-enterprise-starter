package com.aistarter.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatStreamRequest {

    @NotBlank
    private String message;

    private String sessionId = "default";

    private boolean enableTools = true;
}
