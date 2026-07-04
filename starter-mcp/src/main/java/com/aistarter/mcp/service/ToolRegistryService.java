package com.aistarter.mcp.service;

import com.aistarter.mcp.dto.ToolInfo;
import com.aistarter.mcp.tool.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolRegistryService {

    private final List<Tool> tools;

    public ToolRegistryService(List<Tool> tools) {
        this.tools = tools;
    }

    public List<ToolInfo> listTools() {
        return tools.stream()
                .map(tool -> new ToolInfo(tool.getName(), tool.getDescription(), tool.isReadOnly()))
                .toList();
    }

    public List<Tool> getTools(boolean enabled) {
        return enabled ? tools : List.of();
    }

    public Tool findByName(String name) {
        return tools.stream()
                .filter(tool -> tool.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + name));
    }
}
