package com.aistarter.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DatabaseAgentRequest {

    @NotBlank
    private String question;
}
