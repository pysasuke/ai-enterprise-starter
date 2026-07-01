package com.aistarter.rag.dto;

import com.aistarter.rag.entity.RagDocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RagDocumentListItem {

    private Long id;
    private String filename;
    private String contentType;
    private int chunkCount;
    private RagDocumentStatus status;
    private LocalDateTime createdAt;
}
