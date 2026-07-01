package com.aistarter.rag.entity;

import com.aistarter.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rag_document")
public class RagDocument extends BaseEntity {

    public static final String DEFAULT_COLLECTION = "default";

    @Column(name = "collection_id", nullable = false, length = 64)
    private String collectionId = DEFAULT_COLLECTION;

    @Column(nullable = false, length = 512)
    private String filename;

    @Column(name = "content_type", nullable = false, length = 128)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RagDocumentStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
