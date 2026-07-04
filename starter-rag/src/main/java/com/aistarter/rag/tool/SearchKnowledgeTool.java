package com.aistarter.rag.tool;

import com.aistarter.mcp.tool.Tool;
import com.aistarter.mcp.tool.ToolExecutionResult;
import com.aistarter.rag.entity.RagDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SearchKnowledgeTool implements Tool {

    private final VectorStore vectorStore;

    @Override
    public String getName() {
        return "searchKnowledge";
    }

    @Override
    public String getDescription() {
        return "Search the knowledge base and return the most relevant document snippets.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "query", Map.of("type", "string"),
                        "topK", Map.of("type", "integer")),
                "required", List.of("query"));
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> arguments) {
        try {
            String query = String.valueOf(arguments.get("query"));
            int topK = arguments.get("topK") instanceof Number number ? number.intValue() : 3;
            List<Document> matches = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .filterExpression("collection_id == '" + RagDocument.DEFAULT_COLLECTION + "'")
                    .build());
            return ToolExecutionResult.ok(formatMatches(matches));
        } catch (Exception e) {
            return ToolExecutionResult.fail(e.getMessage());
        }
    }

    private String formatMatches(List<Document> matches) {
        if (matches.isEmpty()) {
            return "No matching documents found.";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < matches.size(); i++) {
            Document match = matches.get(i);
            String filename = String.valueOf(match.getMetadata().getOrDefault("filename", "unknown"));
            builder.append("[").append(i + 1).append("] ").append(filename).append(": ")
                    .append(match.getText()).append("\n\n");
        }
        return builder.toString().trim();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
