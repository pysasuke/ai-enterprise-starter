package com.aistarter.prompt.service;

import com.aistarter.prompt.entity.PromptType;

public final class PromptFallbacks {

    public static final String KEY_CHAT = "chat.system";
    public static final String KEY_DATABASE_AGENT = "database.agent";
    public static final String KEY_RAG_CHAT = "rag.chat";

    private static final String CHAT_SYSTEM = """
            你是企业 AI 助手，回答应简洁、准确、有帮助。
            """;

    private static final String DATABASE_AGENT_SYSTEM = """
            你是一名资深数据库性能分析专家。
            根据用户提供的数据库 Schema、索引信息和问题，给出 SQL 优化建议。
            只做分析，不要生成或执行任何 DDL/DML。
            输出应包含：问题原因、索引建议、查询改写建议（如有）。
            """;

    private static final String DATABASE_AGENT_USER = """
            用户问题：{{question}}

            数据库 Schema：
            {{schema}}

            现有索引：
            {{indexes}}

            请给出分析和优化建议。
            """;

    private static final String RAG_CHAT_SYSTEM = """
            你是企业知识库问答助手。仅根据提供的检索上下文回答问题。
            如果上下文中没有足够信息，请明确说明不知道，不要编造。
            回答应简洁、准确，必要时引用上下文中的要点。
            """;

    private static final String RAG_CHAT_USER = """
            检索到的上下文：
            {{context}}

            用户问题：{{question}}
            """;

    private PromptFallbacks() {
    }

    public static String fallback(String promptKey, PromptType type) {
        if (KEY_CHAT.equals(promptKey) && type == PromptType.system) {
            return CHAT_SYSTEM;
        }
        if (KEY_DATABASE_AGENT.equals(promptKey)) {
            return type == PromptType.system ? DATABASE_AGENT_SYSTEM : DATABASE_AGENT_USER;
        }
        if (KEY_RAG_CHAT.equals(promptKey)) {
            return type == PromptType.system ? RAG_CHAT_SYSTEM : RAG_CHAT_USER;
        }
        return "";
    }
}
