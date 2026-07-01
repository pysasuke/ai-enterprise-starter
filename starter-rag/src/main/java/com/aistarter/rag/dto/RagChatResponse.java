package com.aistarter.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RagChatResponse {

    private String answer;
    private List<RagSource> sources;
}
