package com.aistarter.rag.service;

import com.aistarter.rag.dto.RagChatRequest;
import com.aistarter.rag.dto.RagChatResponse;
import com.aistarter.rag.dto.RagSource;
import com.aistarter.rag.entity.RagDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RagChatService {

    private static final String SYSTEM_PROMPT = """
            你是企业知识库问答助手。仅根据提供的检索上下文回答问题。
            如果上下文中没有足够信息，请明确说明不知道，不要编造。
            回答应简洁、准确，必要时引用上下文中的要点。
            """;

    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;

    public RagChatResponse chat(RagChatRequest request) {
        int topK = request.getTopK() > 0 ? request.getTopK() : 5;

        List<Document> matches = vectorStore.similaritySearch(SearchRequest.builder()
                .query(request.getQuestion())
                .topK(topK)
                .filterExpression("collection_id == '" + RagDocument.DEFAULT_COLLECTION + "'")
                .build());

        String context = buildContext(matches);
        String userPrompt = """
                检索到的上下文：
                %s

                用户问题：%s
                """.formatted(context, request.getQuestion());

        String answer = chatClientBuilder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();

        return new RagChatResponse(answer, buildSources(matches));
    }

    private String buildContext(List<Document> matches) {
        if (matches.isEmpty()) {
            return "（无检索结果）";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < matches.size(); i++) {
            builder.append("[").append(i + 1).append("] ").append(matches.get(i).getText()).append("\n\n");
        }
        return builder.toString().trim();
    }

    private List<RagSource> buildSources(List<Document> matches) {
        Map<String, RagSource> deduped = new LinkedHashMap<>();
        for (Document match : matches) {
            Map<String, Object> metadata = match.getMetadata();
            String documentIdStr = String.valueOf(metadata.getOrDefault("document_id", ""));
            String filename = String.valueOf(metadata.getOrDefault("filename", "unknown"));
            String key = documentIdStr + "|" + filename;
            if (deduped.containsKey(key)) {
                continue;
            }
            Long documentId = documentIdStr.isBlank() ? null : Long.valueOf(documentIdStr);
            String snippet = match.getText();
            if (snippet.length() > 200) {
                snippet = snippet.substring(0, 200) + "...";
            }
            deduped.put(key, new RagSource(documentId, filename, snippet));
        }
        return new ArrayList<>(deduped.values());
    }
}
