package com.aistarter.rag.dto;

import com.aistarter.rag.entity.RagDocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RagUploadResponse {

    private Long id;
    private String filename;
    private String contentType;
    private int chunkCount;
    private RagDocumentStatus status;
}
