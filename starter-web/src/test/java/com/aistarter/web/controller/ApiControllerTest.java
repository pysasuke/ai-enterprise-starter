package com.aistarter.web.controller;

import com.aistarter.agent.dto.DatabaseAgentResponse;
import com.aistarter.agent.service.DatabaseAnalyzeAgent;
import com.aistarter.ai.dto.ChatResponse;
import com.aistarter.ai.service.ChatService;
import com.aistarter.ai.service.StreamChatService;
import com.aistarter.ai.stream.SseEvent;
import com.aistarter.auth.dto.LoginResponse;
import com.aistarter.auth.service.AuthService;
import com.aistarter.mcp.dto.ToolInfo;
import com.aistarter.mcp.service.ToolRegistryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {ApiController.class})
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AuthService authService;
    @MockBean
    private ChatService chatService;
    @MockBean
    private StreamChatService streamChatService;
    @MockBean
    private ToolRegistryService toolRegistryService;
    @MockBean
    private DatabaseAnalyzeAgent databaseAnalyzeAgent;

    @Test
    void chatEndpointShouldReturnContent() throws Exception {
        when(chatService.chat(any())).thenReturn(new ChatResponse("你好，我是AI助手。"));
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"你好\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("你好，我是AI助手。"));
    }

    @Test
    void toolsEndpointShouldReturnList() throws Exception {
        when(toolRegistryService.listTools()).thenReturn(List.of(
                new ToolInfo("calculateArea", "Calculate area", true)));
        mockMvc.perform(get("/api/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("calculateArea"));
    }

    @Test
    void chatStreamEndpointShouldReturnSseEvents() throws Exception {
        when(streamChatService.chatStream(any())).thenReturn(Flux.just(
                SseEvent.start("conv-1"),
                SseEvent.chunk("hello"),
                SseEvent.done(10, Map.of())));

        mockMvc.perform(post("/api/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .content("{\"message\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("\"type\":\"chunk\"")));
    }

    @Test
    void databaseAgentEndpointShouldReturnAnalysis() throws Exception {
        when(databaseAnalyzeAgent.analyze(any())).thenReturn(new DatabaseAgentResponse("缺少索引"));
        mockMvc.perform(post("/api/agent/database")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"为什么慢？\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis").value("缺少索引"));
    }

    @Test
    void loginEndpointShouldReturnToken() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("token", "admin"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("token"));
    }
}
