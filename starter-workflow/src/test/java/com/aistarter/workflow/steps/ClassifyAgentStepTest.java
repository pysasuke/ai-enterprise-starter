package com.aistarter.workflow.steps;

import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowKeys;
import com.aistarter.workflow.router.AgentRouteDecision;
import com.aistarter.workflow.router.AgentRouteLlmClassifier;
import com.aistarter.workflow.router.AgentRouteRuleMatcher;
import com.aistarter.workflow.router.AgentType;
import com.aistarter.workflow.router.RouteMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassifyAgentStepTest {

    @Mock
    private AgentRouteRuleMatcher ruleMatcher;
    @Mock
    private AgentRouteLlmClassifier llmClassifier;
    @InjectMocks
    private ClassifyAgentStep step;

    @Test
    void usesRuleWhenMatched() {
        WorkflowContext context = new WorkflowContext();
        context.put(WorkflowKeys.QUESTION, "退款政策是什么？");
        when(ruleMatcher.match("退款政策是什么？")).thenReturn(Optional.of(AgentType.RAG));

        step.execute(context);

        assertThat(context.<AgentType>get(WorkflowKeys.SELECTED_AGENT)).isEqualTo(AgentType.RAG);
        assertThat(context.<RouteMethod>get(WorkflowKeys.ROUTE_METHOD)).isEqualTo(RouteMethod.RULE);
        verifyNoInteractions(llmClassifier);
    }

    @Test
    void usesLlmWhenRuleMisses() {
        WorkflowContext context = new WorkflowContext();
        context.put(WorkflowKeys.QUESTION, "explain quantum physics");
        when(ruleMatcher.match("explain quantum physics")).thenReturn(Optional.empty());
        when(llmClassifier.classify("explain quantum physics"))
                .thenReturn(new AgentRouteDecision(AgentType.CHAT, RouteMethod.LLM));

        step.execute(context);

        assertThat(context.<AgentType>get(WorkflowKeys.SELECTED_AGENT)).isEqualTo(AgentType.CHAT);
        assertThat(context.<RouteMethod>get(WorkflowKeys.ROUTE_METHOD)).isEqualTo(RouteMethod.LLM);
    }
}
