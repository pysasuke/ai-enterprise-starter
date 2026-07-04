package com.aistarter.agent.tool;

import com.aistarter.agent.dto.DatabaseAgentRequest;
import com.aistarter.agent.service.DatabaseAnalyzeAgent;
import com.aistarter.mcp.tool.Tool;
import com.aistarter.mcp.tool.ToolExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryDatabaseTool implements Tool {

    private final DatabaseAnalyzeAgent databaseAnalyzeAgent;

    @Override
    public String getName() {
        return "queryDatabase";
    }

    @Override
    public String getDescription() {
        return "Analyze database schema and suggest SQL optimizations from a natural language question.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of("question", Map.of("type", "string")),
                "required", List.of("question"));
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> arguments) {
        try {
            String question = String.valueOf(arguments.get("question"));
            DatabaseAgentRequest request = new DatabaseAgentRequest();
            request.setQuestion(question);
            var response = databaseAnalyzeAgent.analyze(request);
            return ToolExecutionResult.ok(response.getAnalysis());
        } catch (Exception e) {
            return ToolExecutionResult.fail(e.getMessage());
        }
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
