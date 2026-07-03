package com.aistarter.rag.service;

import com.aistarter.common.exception.BusinessException;
import com.aistarter.rag.parser.ParseResult;
import com.aistarter.rag.entity.RagDocument;
import com.aistarter.rag.entity.RagDocumentStatus;
import com.aistarter.rag.parser.DocumentParserFactory;
import com.aistarter.rag.repository.RagDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagDocumentServiceTest {

    @Mock
    private RagDocumentRepository documentRepository;

    @Mock
    private DocumentParserFactory parserFactory;

    @Mock
    private TokenTextSplitter textSplitter;

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private RagDocumentService ragDocumentService;

    @Test
    void ingestIndexesDocument() throws Exception {
        RagDocument saved = new RagDocument();
        saved.setId(1L);
        saved.setFilename("policy.md");
        saved.setContentType("text/markdown");
        saved.setFileSize(100);
        saved.setStatus(RagDocumentStatus.INDEXING);

        when(documentRepository.save(any(RagDocument.class))).thenReturn(saved);
        when(parserFactory.parse(any(), any(), any()))
                .thenReturn(ParseResult.of("Refund within 7 days", Map.of("ocr_used", false, "parser", "txt-md")));
        when(textSplitter.split(any(Document.class))).thenReturn(List.of(new Document("chunk-1")));

        var response = ragDocumentService.ingest(
                "policy.md",
                "text/markdown",
                100,
                new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8)));

        assertThat(response.getChunkCount()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo(RagDocumentStatus.READY);
        verify(vectorStore).add(anyList());
    }

    @Test
    void rejectsOversizedFile() {
        assertThatThrownBy(() -> ragDocumentService.ingest(
                "big.pdf", "application/pdf", 11L * 1024 * 1024, new ByteArrayInputStream(new byte[0])))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("10MB");
    }
}
