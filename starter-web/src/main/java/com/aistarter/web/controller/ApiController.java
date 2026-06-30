package com.aistarter.web.controller;

import com.aistarter.agent.dto.DatabaseAgentRequest;
import com.aistarter.agent.dto.DatabaseAgentResponse;
import com.aistarter.agent.service.DatabaseAnalyzeAgent;
import com.aistarter.ai.dto.ChatRequest;
import com.aistarter.ai.dto.ChatResponse;
import com.aistarter.ai.service.ChatService;
import com.aistarter.auth.dto.LoginRequest;
import com.aistarter.auth.dto.LoginResponse;
import com.aistarter.auth.service.AuthService;
import com.aistarter.common.constant.AppConstants;
import com.aistarter.common.result.Result;
import com.aistarter.mcp.service.ToolRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_PREFIX)
@RequiredArgsConstructor
@Tag(name = "AI Enterprise API")
public class ApiController {

    private final AuthService authService;
    private final ChatService chatService;
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

    @GetMapping("/tools")
    @Operation(summary = "Tool 列表")
    public List<String> listTools() {
        return toolRegistryService.listTools();
    }

    @PostMapping("/agent/database")
    @Operation(summary = "Database Analyze Agent")
    public DatabaseAgentResponse analyzeDatabase(@Valid @RequestBody DatabaseAgentRequest request) {
        return databaseAnalyzeAgent.analyze(request);
    }
}
