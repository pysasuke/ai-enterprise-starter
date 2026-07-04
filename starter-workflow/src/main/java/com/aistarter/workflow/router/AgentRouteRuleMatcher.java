package com.aistarter.workflow.router;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class AgentRouteRuleMatcher {

    private static final String[] DATABASE_KEYWORDS = {
            "表", "索引", "schema", "sql", "慢查询", "query slow", "user_id", "postgres", "mysql"
    };
    private static final String[] RAG_KEYWORDS = {
            "政策", "文档", "知识库", "退款", "refund", "policy", "上传", "知识"
    };

    public Optional<AgentType> match(String question) {
        if (question == null || question.isBlank()) {
            return Optional.empty();
        }
        String lower = question.toLowerCase(Locale.ROOT);
        if (containsAny(lower, DATABASE_KEYWORDS)) {
            return Optional.of(AgentType.DATABASE);
        }
        if (containsAny(lower, RAG_KEYWORDS)) {
            return Optional.of(AgentType.RAG);
        }
        return Optional.empty();
    }

    private boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
