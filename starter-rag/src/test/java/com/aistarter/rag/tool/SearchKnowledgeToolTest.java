package com.aistarter.rag.tool;

import com.aistarter.mcp.tool.ToolExecutionResult;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchKnowledgeToolTest {

    @Test
    void returnsFormattedSnippets() {
        VectorStore vectorStore = mock(VectorStore.class);
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(new Document("Refund within 7 days", Map.of("filename", "refund-policy.md"))));
        SearchKnowledgeTool tool = new SearchKnowledgeTool(vectorStore);

        ToolExecutionResult result = tool.execute(Map.of("query", "refund policy"));

        assertTrue(result.success());
        assertTrue(result.result().contains("refund-policy.md"));
        assertTrue(result.result().contains("Refund within 7 days"));
    }
}
