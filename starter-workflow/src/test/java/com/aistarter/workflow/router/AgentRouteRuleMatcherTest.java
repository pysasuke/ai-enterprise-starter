package com.aistarter.workflow.router;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AgentRouteRuleMatcherTest {

    private final AgentRouteRuleMatcher matcher = new AgentRouteRuleMatcher();

    @Test
    void matchesDatabaseKeywords() {
        assertThat(matcher.match("orders表按user_id查询为什么慢？"))
                .contains(AgentType.DATABASE);
    }

    @Test
    void matchesRagKeywords() {
        assertThat(matcher.match("退款政策是什么？"))
                .contains(AgentType.RAG);
    }

    @Test
    void databaseWinsWhenBothMatch() {
        assertThat(matcher.match("orders表的退款政策文档里怎么写？"))
                .contains(AgentType.DATABASE);
    }

    @Test
    void returnsEmptyWhenNoMatch() {
        assertThat(matcher.match("你好")).isEmpty();
    }
}
