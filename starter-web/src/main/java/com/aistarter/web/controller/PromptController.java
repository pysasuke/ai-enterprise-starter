package com.aistarter.web.controller;

import com.aistarter.common.constant.AppConstants;
import com.aistarter.prompt.dto.CreatePromptVersionRequest;
import com.aistarter.prompt.dto.CreatePromptVersionResponse;
import com.aistarter.prompt.dto.PromptDefinitionResponse;
import com.aistarter.prompt.dto.PromptRenderRequest;
import com.aistarter.prompt.dto.PromptRenderResponse;
import com.aistarter.prompt.dto.PromptVersionResponse;
import com.aistarter.prompt.dto.SetActiveVersionRequest;
import com.aistarter.prompt.entity.PromptType;
import com.aistarter.prompt.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/prompts")
@RequiredArgsConstructor
@Tag(name = "Prompt Management")
public class PromptController {

    private final PromptService promptService;

    @GetMapping
    @Operation(summary = "列出所有 Prompt 定义")
    public List<PromptDefinitionResponse> listDefinitions() {
        return promptService.listDefinitions();
    }

    @GetMapping("/{key}/{type}")
    @Operation(summary = "获取 Prompt 定义及当前生效内容摘要")
    public PromptDefinitionResponse getDefinition(@PathVariable String key, @PathVariable PromptType type) {
        return promptService.getDefinition(key, type);
    }

    @GetMapping("/{key}/{type}/versions")
    @Operation(summary = "列出 Prompt 版本历史")
    public List<PromptVersionResponse> listVersions(@PathVariable String key, @PathVariable PromptType type) {
        return promptService.listVersions(key, type);
    }

    @PostMapping("/{key}/{type}/versions")
    @Operation(summary = "创建 Prompt 新版本")
    public CreatePromptVersionResponse createVersion(
            @PathVariable String key,
            @PathVariable PromptType type,
            @Valid @RequestBody CreatePromptVersionRequest request) {
        return promptService.createVersion(key, type, request.getContent(), request.getDescription());
    }

    @PutMapping("/{key}/{type}/active")
    @Operation(summary = "设置当前生效版本")
    public PromptDefinitionResponse setActiveVersion(
            @PathVariable String key,
            @PathVariable PromptType type,
            @Valid @RequestBody SetActiveVersionRequest request) {
        return promptService.setActiveVersion(key, type, request.getVersion());
    }

    @PostMapping("/render")
    @Operation(summary = "预览渲染 Prompt 模板")
    public PromptRenderResponse render(@Valid @RequestBody PromptRenderRequest request) {
        return promptService.renderForApi(
                request.getKey(),
                request.getType(),
                request.getVersion(),
                request.getVariables());
    }

    @DeleteMapping("/{key}/{type}/versions/{version}")
    @Operation(summary = "删除非生效 Prompt 版本")
    public void deleteVersion(
            @PathVariable String key,
            @PathVariable PromptType type,
            @PathVariable int version) {
        promptService.deleteVersion(key, type, version);
    }
}
