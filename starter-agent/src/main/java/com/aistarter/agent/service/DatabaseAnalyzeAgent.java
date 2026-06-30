package com.aistarter.agent.service;

import com.aistarter.agent.dto.DatabaseAgentRequest;
import com.aistarter.agent.dto.DatabaseAgentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseAnalyzeAgent {

    private static final String SYSTEM_PROMPT = """
            你是一名资深数据库性能分析专家。
            根据用户提供的数据库 Schema、索引信息和问题，给出 SQL 优化建议。
            只做分析，不要生成或执行任何 DDL/DML。
            输出应包含：问题原因、索引建议、查询改写建议（如有）。
            """;

    private final ChatClient.Builder chatClientBuilder;
    private final DatabaseSchemaService schemaService;

    public DatabaseAgentResponse analyze(DatabaseAgentRequest request) {
        String schema = schemaService.loadSchemaSummary();
        String indexes = schemaService.loadIndexSummary();

        String userPrompt = """
                用户问题：%s

                数据库 Schema：
                %s

                现有索引：
                %s

                请给出分析和优化建议。
                """.formatted(request.getQuestion(), schema, indexes);

        String analysis = chatClientBuilder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .content();

        return new DatabaseAgentResponse(analysis);
    }
}
