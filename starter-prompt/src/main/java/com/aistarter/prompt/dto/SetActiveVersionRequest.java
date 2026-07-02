package com.aistarter.prompt.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SetActiveVersionRequest {

    @Min(1)
    private int version;
}
