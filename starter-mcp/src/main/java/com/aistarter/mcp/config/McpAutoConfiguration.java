package com.aistarter.mcp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.ai.mcp.client.enabled", havingValue = "true", matchIfMissing = false)
public class McpAutoConfiguration {
}
