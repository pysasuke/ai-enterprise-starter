package com.aistarter.workflow.router;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentRouteLlmClassifierTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    private ChatClient.CallResponseSpec responseSpec;
    @InjectMocks
    private AgentRouteLlmClassifier classifier;

    @Test
    void parsesRagFromLlmResponse() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("RAG");

        AgentRouteDecision decision = classifier.classify("tell me about refunds");

        assertThat(decision.agentType()).isEqualTo(AgentType.RAG);
        assertThat(decision.routeMethod()).isEqualTo(RouteMethod.LLM);
    }

    @Test
    void fallsBackToChatOnInvalidResponse() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("UNKNOWN");

        AgentRouteDecision decision = classifier.classify("hello");

        assertThat(decision.agentType()).isEqualTo(AgentType.CHAT);
        assertThat(decision.routeMethod()).isEqualTo(RouteMethod.FALLBACK);
    }
}
