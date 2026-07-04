package com.aistarter.ai.stream;

import com.aistarter.mcp.service.ToolExecutor;
import com.aistarter.mcp.service.ToolRegistryService;
import com.aistarter.mcp.support.ToolCallbackFactory;
import com.aistarter.mcp.tool.ToolExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class StreamToolChatOrchestrator {

    private final ToolRegistryService toolRegistryService;
    private final ToolExecutor toolExecutor;

    public Flux<SseEvent> stream(
            String conversationId,
            ChatClient.Builder chatClientBuilder,
            String systemPrompt,
            String userPrompt,
            boolean enableTools,
            Map<String, Object> doneMetadata) {

        return Flux.create(sink -> Schedulers.boundedElastic().schedule(() ->
                runStream(conversationId, chatClientBuilder, systemPrompt, userPrompt, enableTools, doneMetadata, sink)));
    }

    private void runStream(
            String conversationId,
            ChatClient.Builder chatClientBuilder,
            String systemPrompt,
            String userPrompt,
            boolean enableTools,
            Map<String, Object> doneMetadata,
            FluxSink<SseEvent> sink) {

        long startedAt = System.currentTimeMillis();
        try {
            sink.next(SseEvent.start(conversationId));

            List<SseEvent> sideEvents = Collections.synchronizedList(new ArrayList<>());
            List<ToolCallback> toolCallbacks = enableTools
                    ? ToolCallbackFactory.from(
                            toolRegistryService.getTools(true),
                            toolExecutor,
                            new ToolCallbackFactory.ToolExecutionListener() {
                                @Override
                                public void onToolCall(String toolId, String toolName, Map<String, Object> arguments) {
                                    sideEvents.add(SseEvent.toolCall(toolId, toolName, arguments));
                                }

                                @Override
                                public void onToolResult(String toolId, String toolName, ToolExecutionResult result) {
                                    sideEvents.add(SseEvent.toolResult(
                                            toolId, toolName, result.success(), result.success() ? result.result() : result.error()));
                                }
                            })
                    : List.of();

            AtomicReference<String> emittedText = new AtomicReference<>("");

            var spec = chatClientBuilder.build().prompt();
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                spec = spec.system(systemPrompt);
            }
            if (!toolCallbacks.isEmpty()) {
                spec = spec.toolCallbacks(toolCallbacks);
            }

            spec.user(userPrompt)
                    .stream()
                    .chatResponse()
                    .doOnNext(response -> handleResponseChunk(response, emittedText, sideEvents, sink))
                    .blockLast();

            flushSideEvents(sideEvents, sink);
            sink.next(SseEvent.done(System.currentTimeMillis() - startedAt, doneMetadata));
            sink.complete();
        } catch (Exception e) {
            sink.next(SseEvent.error("STREAM_FAILED", e.getMessage(), null));
            sink.complete();
        }
    }

    private void handleResponseChunk(
            ChatResponse response,
            AtomicReference<String> emittedText,
            List<SseEvent> sideEvents,
            FluxSink<SseEvent> sink) {

        flushSideEvents(sideEvents, sink);

        if (response.getResult() == null || response.getResult().getOutput() == null) {
            return;
        }
        if (!(response.getResult().getOutput() instanceof AssistantMessage assistantMessage)) {
            return;
        }

        String currentText = assistantMessage.getText() == null ? "" : assistantMessage.getText();

        String alreadyEmitted = emittedText.get();
        if (currentText.length() > alreadyEmitted.length()) {
            String delta = currentText.substring(alreadyEmitted.length());
            emittedText.set(currentText);
            sink.next(SseEvent.chunk(delta));
        }
    }

    private void flushSideEvents(List<SseEvent> sideEvents, FluxSink<SseEvent> sink) {
        if (sideEvents.isEmpty()) {
            return;
        }
        sideEvents.forEach(sink::next);
        sideEvents.clear();
    }
}
