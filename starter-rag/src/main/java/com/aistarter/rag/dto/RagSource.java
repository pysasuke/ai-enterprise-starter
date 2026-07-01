package com.aistarter.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RagSource {

    private Long documentId;
    private String filename;
    private String snippet;
}
