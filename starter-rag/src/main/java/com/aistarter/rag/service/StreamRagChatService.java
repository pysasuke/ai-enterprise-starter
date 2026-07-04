package com.aistarter.rag.service;

import com.aistarter.ai.stream.SseEvent;
import com.aistarter.ai.stream.StreamToolChatOrchestrator;
import com.aistarter.prompt.entity.PromptType;
import com.aistarter.prompt.service.PromptFallbacks;
import com.aistarter.prompt.service.PromptService;
import com.aistarter.rag.dto.RagChatRequest;
import com.aistarter.rag.dto.RagSource;
import com.aistarter.rag.entity.RagDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StreamRagChatService {

    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;
    private final PromptService promptService;
    private final StreamToolChatOrchestrator orchestrator;

    public Flux<SseEvent> chatStream(RagChatRequest request) {
        int topK = request.getTopK() > 0 ? request.getTopK() : 5;
        boolean enableTools = request.isEnableTools();

        List<Document> matches = vectorStore.similaritySearch(SearchRequest.builder()
                .query(request.getQuestion())
                .topK(topK)
                .filterExpression("collection_id == '" + RagDocument.DEFAULT_COLLECTION + "'")
                .build());

        String context = buildContext(matches);
        Map<String, Object> variables = Map.of(
                "context", context,
                "question", request.getQuestion());

        String systemPrompt = promptService.render(PromptFallbacks.KEY_RAG_CHAT, PromptType.system, Map.of());
        String userPrompt = promptService.render(PromptFallbacks.KEY_RAG_CHAT, PromptType.user, variables);
        List<RagSource> sources = buildSources(matches);
        Map<String, Object> doneMetadata = Map.of("sources", sources);

        return orchestrator.stream(
                SseEvent.newConversationId(),
                chatClientBuilder,
                systemPrompt,
                userPrompt,
                enableTools,
                doneMetadata);
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
