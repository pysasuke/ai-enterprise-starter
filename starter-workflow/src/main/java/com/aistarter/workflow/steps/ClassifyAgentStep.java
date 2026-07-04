package com.aistarter.workflow.steps;

import com.aistarter.workflow.engine.StepResult;
import com.aistarter.workflow.engine.WorkflowContext;
import com.aistarter.workflow.engine.WorkflowKeys;
import com.aistarter.workflow.engine.WorkflowStep;
import com.aistarter.workflow.router.AgentRouteDecision;
import com.aistarter.workflow.router.AgentRouteLlmClassifier;
import com.aistarter.workflow.router.AgentRouteRuleMatcher;
import com.aistarter.workflow.router.AgentType;
import com.aistarter.workflow.router.RouteMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClassifyAgentStep implements WorkflowStep {

    private final AgentRouteRuleMatcher ruleMatcher;
    private final AgentRouteLlmClassifier llmClassifier;

    @Override
    public String name() {
        return "classify";
    }

    @Override
    public StepResult execute(WorkflowContext context) {
        String question = context.getString(WorkflowKeys.QUESTION);
        Optional<AgentType> ruleMatch = ruleMatcher.match(question);
        AgentType selected;
        RouteMethod method;
        if (ruleMatch.isPresent()) {
            selected = ruleMatch.get();
            method = RouteMethod.RULE;
        } else {
            AgentRouteDecision decision = llmClassifier.classify(question);
            selected = decision.agentType();
            method = decision.routeMethod();
        }
        context.put(WorkflowKeys.SELECTED_AGENT, selected);
        context.put(WorkflowKeys.ROUTE_METHOD, method);
        return new StepResult(method.name() + ":" + selected.name());
    }
}
