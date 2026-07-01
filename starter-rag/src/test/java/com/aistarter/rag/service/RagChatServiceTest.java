package com.aistarter.rag.service;

import com.aistarter.rag.dto.RagChatRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagChatServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    @InjectMocks
    private RagChatService ragChatService;

    @Test
    void chatReturnsAnswerAndSources() {
        Document match = new Document("Refund within 7 days", Map.of(
                "document_id", "1",
                "filename", "policy.md",
                "collection_id", "default"));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(match));
        when(chatClientBuilder.build().prompt().system(any(String.class)).user(any(String.class)).call().content())
                .thenReturn("You can refund within 7 days.");

        RagChatRequest request = new RagChatRequest();
        request.setQuestion("退款政策是什么？");
        request.setTopK(3);
        var response = ragChatService.chat(request);

        assertThat(response.getAnswer()).contains("7 days");
        assertThat(response.getSources()).hasSize(1);
        assertThat(response.getSources().getFirst().getFilename()).isEqualTo("policy.md");
    }
}
