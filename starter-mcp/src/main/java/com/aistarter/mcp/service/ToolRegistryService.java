package com.aistarter.mcp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ToolRegistryService {

    public List<String> listTools() {
        Set<String> tools = new LinkedHashSet<>();
        tools.add("database");
        tools.add("filesystem");
        return new ArrayList<>(tools);
    }
}
