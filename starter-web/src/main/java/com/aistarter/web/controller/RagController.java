package com.aistarter.web.controller;

import com.aistarter.common.constant.AppConstants;
import com.aistarter.rag.dto.RagChatRequest;
import com.aistarter.rag.dto.RagChatResponse;
import com.aistarter.rag.dto.RagDocumentListItem;
import com.aistarter.rag.dto.RagUploadResponse;
import com.aistarter.rag.service.RagChatService;
import com.aistarter.rag.service.RagDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/rag")
@RequiredArgsConstructor
@Tag(name = "RAG Knowledge Base")
public class RagController {

    private final RagDocumentService ragDocumentService;
    private final RagChatService ragChatService;

    @PostMapping("/documents")
    @Operation(summary = "上传文档到知识库")
    public RagUploadResponse uploadDocument(@RequestPart("file") MultipartFile file) throws IOException {
        return ragDocumentService.ingest(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream());
    }

    @GetMapping("/documents")
    @Operation(summary = "列出已入库文档")
    public List<RagDocumentListItem> listDocuments() {
        return ragDocumentService.listDocuments();
    }

    @DeleteMapping("/documents/{id}")
    @Operation(summary = "删除文档及向量")
    public void deleteDocument(@PathVariable Long id) {
        ragDocumentService.deleteDocument(id);
    }

    @PostMapping("/chat")
    @Operation(summary = "RAG 知识库问答")
    public RagChatResponse chat(@Valid @RequestBody RagChatRequest request) {
        return ragChatService.chat(request);
    }
}
