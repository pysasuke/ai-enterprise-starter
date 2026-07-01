package com.aistarter.rag.service;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.rag.dto.RagDocumentListItem;
import com.aistarter.rag.dto.RagUploadResponse;
import com.aistarter.rag.entity.RagDocument;
import com.aistarter.rag.entity.RagDocumentStatus;
import com.aistarter.rag.parser.DocumentParserFactory;
import com.aistarter.rag.repository.RagDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RagDocumentService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final RagDocumentRepository documentRepository;
    private final DocumentParserFactory parserFactory;
    private final TokenTextSplitter textSplitter;
    private final VectorStore vectorStore;

    @Transactional
    public RagUploadResponse ingest(String filename, String contentType, long fileSize, InputStream inputStream)
            throws IOException {
        if (fileSize <= 0 || fileSize > MAX_FILE_SIZE) {
            throw new BusinessException("File size must be between 1 byte and 10MB");
        }

        RagDocument document = new RagDocument();
        document.setFilename(filename);
        document.setContentType(contentType != null ? contentType : "application/octet-stream");
        document.setFileSize(fileSize);
        document.setCollectionId(RagDocument.DEFAULT_COLLECTION);
        document.setStatus(RagDocumentStatus.INDEXING);
        document.setChunkCount(0);
        document = documentRepository.save(document);

        try {
            String text = parserFactory.parse(document.getContentType(), filename, inputStream);
            if (text.isBlank()) {
                throw new BusinessException("Document contains no extractable text");
            }

            List<Document> chunks = new ArrayList<>(textSplitter.split(new Document(text)));
            if (chunks.isEmpty()) {
                throw new BusinessException("Document produced no chunks after splitting");
            }

            String documentId = document.getId().toString();
            List<Document> enrichedChunks = new ArrayList<>();
            for (Document chunk : chunks) {
                Map<String, Object> metadata = new java.util.HashMap<>(chunk.getMetadata());
                metadata.put("document_id", documentId);
                metadata.put("filename", filename);
                metadata.put("collection_id", RagDocument.DEFAULT_COLLECTION);
                String chunkId = chunk.getId() != null ? chunk.getId() : UUID.randomUUID().toString();
                enrichedChunks.add(new Document(chunkId, chunk.getText(), metadata));
            }

            vectorStore.add(enrichedChunks);

            document.setChunkCount(enrichedChunks.size());
            document.setStatus(RagDocumentStatus.READY);
            documentRepository.save(document);

            return new RagUploadResponse(
                    document.getId(),
                    document.getFilename(),
                    document.getContentType(),
                    document.getChunkCount(),
                    document.getStatus());
        } catch (RuntimeException ex) {
            document.setStatus(RagDocumentStatus.FAILED);
            document.setErrorMessage(ex.getMessage());
            documentRepository.save(document);
            throw ex;
        }
    }

    public List<RagDocumentListItem> listDocuments() {
        return documentRepository.findAll().stream()
                .map(doc -> new RagDocumentListItem(
                        doc.getId(),
                        doc.getFilename(),
                        doc.getContentType(),
                        doc.getChunkCount(),
                        doc.getStatus(),
                        doc.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void deleteDocument(Long id) {
        RagDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "Document not found: " + id));

        vectorStore.delete(new FilterExpressionBuilder()
                .eq("document_id", document.getId().toString())
                .build());

        documentRepository.delete(document);
    }
}
