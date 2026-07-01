package com.aistarter.rag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RagChatRequest {

    @NotBlank
    private String question;

    private int topK = 5;
}
