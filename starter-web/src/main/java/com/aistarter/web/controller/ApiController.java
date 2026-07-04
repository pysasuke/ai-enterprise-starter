package com.aistarter.web.controller;

import com.aistarter.agent.dto.DatabaseAgentRequest;
import com.aistarter.agent.dto.DatabaseAgentResponse;
import com.aistarter.agent.service.DatabaseAnalyzeAgent;
import com.aistarter.ai.dto.ChatStreamRequest;
import com.aistarter.ai.dto.ChatRequest;
import com.aistarter.ai.dto.ChatResponse;
import com.aistarter.ai.service.ChatService;
import com.aistarter.ai.service.StreamChatService;
import com.aistarter.ai.stream.SseEvent;
import com.aistarter.auth.dto.LoginRequest;
import com.aistarter.auth.dto.LoginResponse;
import com.aistarter.auth.service.AuthService;
import com.aistarter.common.constant.AppConstants;
import com.aistarter.common.result.Result;
import com.aistarter.mcp.dto.ToolInfo;
import com.aistarter.mcp.service.ToolRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_PREFIX)
@RequiredArgsConstructor
@Tag(name = "AI Enterprise API")
public class ApiController {

    private final AuthService authService;
    private final ChatService chatService;
    private final StreamChatService streamChatService;
    private final ToolRegistryService toolRegistryService;
    private final DatabaseAnalyzeAgent databaseAnalyzeAgent;

    @PostMapping("/auth/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/chat")
    @Operation(summary = "AI 聊天")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI 聊天（SSE 流式 + Tool Calling）")
    public Flux<ServerSentEvent<String>> chatStream(@Valid @RequestBody ChatStreamRequest request) {
        return streamChatService.chatStream(request)
                .map(event -> ServerSentEvent.builder(event.toJson()).build());
    }

    @GetMapping("/tools")
    @Operation(summary = "Tool 列表")
    public List<ToolInfo> listTools() {
        return toolRegistryService.listTools();
    }

    @PostMapping("/agent/database")
    @Operation(summary = "Database Analyze Agent")
    public DatabaseAgentResponse analyzeDatabase(@Valid @RequestBody DatabaseAgentRequest request) {
        return databaseAnalyzeAgent.analyze(request);
    }
}
