package com.aistarter.agent.service;

import com.aistarter.agent.dto.DatabaseAgentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseAnalyzeAgentTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private DatabaseSchemaService schemaService;
    @InjectMocks
    private DatabaseAnalyzeAgent databaseAnalyzeAgent;

    @Test
    void analyzeShouldReturnAiSuggestion() {
        when(schemaService.loadSchemaSummary()).thenReturn("schema");
        when(schemaService.loadIndexSummary()).thenReturn("indexes");

        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("建议添加 user_id 索引");

        DatabaseAgentRequest request = new DatabaseAgentRequest();
        request.setQuestion("为什么订单查询慢？");
        var response = databaseAnalyzeAgent.analyze(request);
        assertEquals("建议添加 user_id 索引", response.getAnalysis());
    }
}
