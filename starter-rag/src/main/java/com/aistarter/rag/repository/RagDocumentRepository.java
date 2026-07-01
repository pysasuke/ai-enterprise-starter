package com.aistarter.rag.repository;

import com.aistarter.rag.entity.RagDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RagDocumentRepository extends JpaRepository<RagDocument, Long> {
}
