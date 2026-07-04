# V2 Streaming Chat + Tool Calling — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add SSE streaming endpoints (`/api/chat/stream`, `/api/rag/chat/stream`) with integrated Tool Calling and three read-only tools, without changing existing non-stream APIs.

**Architecture:** Extend `starter-mcp` with a `Tool` contract + `CalculateAreaTool`; place `QueryDatabaseTool` in `starter-agent` and `SearchKnowledgeTool` in `starter-rag` to avoid circular deps. Add `SseEvent` + `StreamToolChatOrchestrator` in `starter-ai` for shared stream/tool loop; `StreamChatService` and `StreamRagChatService` build prompts then delegate to orchestrator. Controllers return `Flux<ServerSentEvent<String>>`.

**Tech Stack:** Java 21, Spring Boot 3.4.5, Spring AI 1.0.0 (ChatClient stream + ToolCallback), Reactor Flux, JUnit 5 + Mockito

**Spec:** `docs/superpowers/specs/2026-07-04-v2-streaming-tool-calling-design.md`

---

## File Map

| File | Action | Responsibility |
|------|--------|----------------|
| `starter-mcp/pom.xml` | Modify | Add `spring-ai-model` for `ToolCallback` |
| `starter-mcp/.../tool/Tool.java` | Create | Tool contract |
| `starter-mcp/.../tool/ToolExecutionResult.java` | Create | Execution result record |
| `starter-mcp/.../dto/ToolInfo.java` | Create | `/api/tools` metadata DTO |
| `starter-mcp/.../tools/CalculateAreaTool.java` | Create | Geometry area tool |
| `starter-mcp/.../service/ToolExecutor.java` | Create | Dispatch by name |
| `starter-mcp/.../service/ToolRegistryService.java` | Modify | Register tools, expose metadata + callbacks |
| `starter-mcp/.../support/ToolCallbackFactory.java` | Create | Wrap `Tool` → Spring AI `ToolCallback` |
| `starter-agent/.../tool/QueryDatabaseTool.java` | Create | Wrap `DatabaseAnalyzeAgent` |
| `starter-rag/pom.xml` | Modify | Add `starter-mcp` dependency |
| `starter-rag/.../tool/SearchKnowledgeTool.java` | Create | Vector search tool |
| `starter-ai/pom.xml` | Modify | Add `starter-mcp`, `spring-boot-starter-webflux` (Reactor) |
| `starter-ai/.../stream/SseEvent.java` | Create | JSON SSE event builder |
| `starter-ai/.../stream/StreamToolChatOrchestrator.java` | Create | Stream + tool loop → `Flux<SseEvent>` |
| `starter-ai/.../dto/ChatStreamRequest.java` | Create | Stream chat request |
| `starter-ai/.../service/StreamChatService.java` | Create | Chat prompt + memory + orchestrator |
| `starter-rag/.../service/StreamRagChatService.java` | Create | RAG retrieval + orchestrator |
| `starter-web/.../controller/ApiController.java` | Modify | `/chat/stream`, `/tools` type |
| `starter-web/.../controller/RagController.java` | Modify | `/chat/stream` |
| `starter-auth/.../SecurityConfig.java` | Modify | permitAll `/api/chat/stream` |
| `scripts/verify.ps1` | Modify | Stream + tools smoke tests |
| `examples/stream-chat-examples.http` | Create | SSE examples |
| `README.md` | Modify | Streaming + Tool Calling section |

**Dependency rule (no cycles):** `starter-mcp` → `starter-common` only. Tool implementations live in `starter-agent` / `starter-rag`. `starter-ai` → `starter-mcp`.

---

## Task 0: Branch setup

**Files:** none (git only)

- [ ] **Step 1: Sync main and create feature branch**

```powershell
cd d:\py\pysasuke
git checkout main
git -c http.version=HTTP/1.1 pull origin main
git checkout -b feature/v0.7-streaming-tools
```

Expected: `git branch --show-current` → `feature/v0.7-streaming-tools`

---

## Task 1: Tool contract + CalculateAreaTool (TDD)

**Files:**
- Modify: `starter-mcp/pom.xml`
- Create: `starter-mcp/src/main/java/com/aistarter/mcp/tool/Tool.java`
- Create: `starter-mcp/src/main/java/com/aistarter/mcp/tool/ToolExecutionResult.java`
- Create: `starter-mcp/src/main/java/com/aistarter/mcp/tools/CalculateAreaTool.java`
- Create: `starter-mcp/src/test/java/com/aistarter/mcp/tools/CalculateAreaToolTest.java`

- [ ] **Step 1: Add spring-ai-model to starter-mcp/pom.xml** (after starter-common)

```xml
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-model</artifactId>
        </dependency>
```

- [ ] **Step 2: Write failing CalculateAreaToolTest**

```java
package com.aistarter.mcp.tools;

import com.aistarter.mcp.tool.ToolExecutionResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalculateAreaToolTest {

    private final CalculateAreaTool tool = new CalculateAreaTool();

    @Test
    void rectangleArea() {
        ToolExecutionResult r = tool.execute(Map.of("shape", "rectangle", "width", 3, "height", 4));
        assertTrue(r.success());
        assertEquals("12.00", r.result());
    }

    @Test
    void triangleArea() {
        ToolExecutionResult r = tool.execute(Map.of("shape", "triangle", "width", 6, "height", 8));
        assertTrue(r.success());
        assertEquals("24.00", r.result());
    }

    @Test
    void circleArea() {
        ToolExecutionResult r = tool.execute(Map.of("shape", "circle", "radius", 2));
        assertTrue(r.success());
        assertEquals(String.format("%.2f", Math.PI * 4), r.result());
    }

    @Test
    void trapezoidArea() {
        ToolExecutionResult r = tool.execute(Map.of(
                "shape", "trapezoid", "topBase", 3, "bottomBase", 5, "height", 4));
        assertTrue(r.success());
        assertEquals("16.00", r.result());
    }

    @Test
    void missingParamsReturnsFailure() {
        ToolExecutionResult r = tool.execute(Map.of("shape", "rectangle", "width", 3));
        assertFalse(r.success());
        assertNotNull(r.error());
    }
}
```

- [ ] **Step 3: Run test — expect FAIL**

```powershell
cd d:\py\pysasuke
mvn -pl starter-mcp test -Dtest=CalculateAreaToolTest -q
```

Expected: compilation failure (classes not found)

- [ ] **Step 4: Create Tool.java and ToolExecutionResult.java**

```java
package com.aistarter.mcp.tool;

import java.util.Map;

public interface Tool {
    String getName();
    String getDescription();
    Map<String, Object> getParametersSchema();
    ToolExecutionResult execute(Map<String, Object> arguments);
    boolean isReadOnly();
}
```

```java
package com.aistarter.mcp.tool;

public record ToolExecutionResult(boolean success, String result, String error) {
    public static ToolExecutionResult ok(String result) {
        return new ToolExecutionResult(true, result, null);
    }
    public static ToolExecutionResult fail(String error) {
        return new ToolExecutionResult(false, null, error);
    }
}
```

- [ ] **Step 5: Implement CalculateAreaTool.java**

```java
package com.aistarter.mcp.tools;

import com.aistarter.mcp.tool.Tool;
import com.aistarter.mcp.tool.ToolExecutionResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CalculateAreaTool implements Tool {

    @Override
    public String getName() {
        return "calculateArea";
    }

    @Override
    public String getDescription() {
        return "Calculate geometric area. Shapes: rectangle, triangle, circle, trapezoid.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "shape", Map.of("type", "string",
                                "enum", List.of("rectangle", "triangle", "circle", "trapezoid")),
                        "width", Map.of("type", "number"),
                        "height", Map.of("type", "number"),
                        "radius", Map.of("type", "number"),
                        "topBase", Map.of("type", "number"),
                        "bottomBase", Map.of("type", "number")),
                "required", List.of("shape"));
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> arguments) {
        try {
            String shape = String.valueOf(arguments.get("shape"));
            double area = switch (shape) {
                case "rectangle" -> num(arguments, "width") * num(arguments, "height");
                case "triangle" -> num(arguments, "width") * num(arguments, "height") / 2.0;
                case "circle" -> Math.PI * Math.pow(num(arguments, "radius"), 2);
                case "trapezoid" -> (num(arguments, "topBase") + num(arguments, "bottomBase"))
                        * num(arguments, "height") / 2.0;
                default -> throw new IllegalArgumentException("Unsupported shape: " + shape);
            };
            return ToolExecutionResult.ok(String.format("%.2f", area));
        } catch (Exception e) {
            return ToolExecutionResult.fail(e.getMessage());
        }
    }

    private static double num(Map<String, Object> args, String key) {
        Object v = args.get(key);
        if (v == null) {
            throw new IllegalArgumentException("Missing parameter: " + key);
        }
        return ((Number) v).doubleValue();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }
}
```

- [ ] **Step 6: Run test — expect PASS**

```powershell
mvn -pl starter-mcp test -Dtest=CalculateAreaToolTest -q
```

- [ ] **Step 7: Commit**

```powershell
git add starter-mcp/pom.xml starter-mcp/src/main/java/com/aistarter/mcp/tool/ starter-mcp/src/main/java/com/aistarter/mcp/tools/ starter-mcp/src/test/java/com/aistarter/mcp/tools/
git commit -m "feat(mcp): add Tool contract and CalculateAreaTool"
```

---

## Task 2: ToolExecutor + ToolRegistryService + ToolInfo

**Files:**
- Create: `starter-mcp/src/main/java/com/aistarter/mcp/dto/ToolInfo.java`
- Create: `starter-mcp/src/main/java/com/aistarter/mcp/service/ToolExecutor.java`
- Modify: `starter-mcp/src/main/java/com/aistarter/mcp/service/ToolRegistryService.java`
- Modify: `starter-mcp/src/test/java/com/aistarter/mcp/service/ToolRegistryServiceTest.java`
- Create: `starter-mcp/src/test/java/com/aistarter/mcp/service/ToolExecutorTest.java`

- [ ] **Step 1: Write failing ToolRegistryServiceTest**

```java
package com.aistarter.mcp.service;

import com.aistarter.mcp.dto.ToolInfo;
import com.aistarter.mcp.tools.CalculateAreaTool;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ToolRegistryServiceTest {

    @Test
    void listToolsReturnsMetadataForAllRegisteredTools() {
        ToolRegistryService service = new ToolRegistryService(List.of(new CalculateAreaTool()));
        List<ToolInfo> tools = service.listTools();
        assertEquals(1, tools.size());
        assertEquals("calculateArea", tools.get(0).name());
        assertTrue(tools.get(0).readOnly());
    }
}
```

- [ ] **Step 2: Run test — expect FAIL**

```powershell
mvn -pl starter-mcp test -Dtest=ToolRegistryServiceTest -q
```

- [ ] **Step 3: Create ToolInfo.java**

```java
package com.aistarter.mcp.dto;

public record ToolInfo(String name, String description, boolean readOnly) {}
```

- [ ] **Step 4: Rewrite ToolRegistryService.java**

```java
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
                .map(t -> new ToolInfo(t.getName(), t.getDescription(), t.isReadOnly()))
                .toList();
    }

    public List<Tool> getTools(boolean enabled) {
        return enabled ? tools : List.of();
    }

    public Tool findByName(String name) {
        return tools.stream()
                .filter(t -> t.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tool: " + name));
    }
}
```

- [ ] **Step 5: Create ToolExecutor.java**

```java
package com.aistarter.mcp.service;

import com.aistarter.mcp.tool.ToolExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ToolExecutor {

    private final ToolRegistryService toolRegistryService;

    public ToolExecutionResult execute(String toolName, Map<String, Object> arguments) {
        try {
            return toolRegistryService.findByName(toolName).execute(arguments);
        } catch (Exception e) {
            return ToolExecutionResult.fail(e.getMessage());
        }
    }
}
```

- [ ] **Step 6: Write ToolExecutorTest and run all starter-mcp tests**

```powershell
mvn -pl starter-mcp test -q
```

Expected: PASS

- [ ] **Step 7: Commit**

```powershell
git add starter-mcp/src/main/java/com/aistarter/mcp/dto/ starter-mcp/src/main/java/com/aistarter/mcp/service/ starter-mcp/src/test/
git commit -m "feat(mcp): add ToolExecutor and upgrade ToolRegistryService"
```

---

## Task 3: QueryDatabaseTool + SearchKnowledgeTool (TDD)

**Files:**
- Create: `starter-agent/src/main/java/com/aistarter/agent/tool/QueryDatabaseTool.java`
- Create: `starter-agent/src/test/java/com/aistarter/agent/tool/QueryDatabaseToolTest.java`
- Modify: `starter-rag/pom.xml`
- Create: `starter-rag/src/main/java/com/aistarter/rag/tool/SearchKnowledgeTool.java`
- Create: `starter-rag/src/test/java/com/aistarter/rag/tool/SearchKnowledgeToolTest.java`

- [ ] **Step 1: Write QueryDatabaseToolTest (mock DatabaseAnalyzeAgent)**

```java
package com.aistarter.agent.tool;

import com.aistarter.agent.dto.DatabaseAgentRequest;
import com.aistarter.agent.dto.DatabaseAgentResponse;
import com.aistarter.agent.service.DatabaseAnalyzeAgent;
import com.aistarter.mcp.tool.ToolExecutionResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QueryDatabaseToolTest {

    @Test
    void delegatesToDatabaseAnalyzeAgent() {
        DatabaseAnalyzeAgent agent = mock(DatabaseAnalyzeAgent.class);
        when(agent.analyze(any())).thenReturn(new DatabaseAgentResponse("add index on user_id"));
        QueryDatabaseTool tool = new QueryDatabaseTool(agent);

        ToolExecutionResult result = tool.execute(Map.of("question", "why slow?"));

        assertTrue(result.success());
        assertEquals("add index on user_id", result.result());
        verify(agent).analyze(new DatabaseAgentRequest("why slow?"));
    }
}
```

- [ ] **Step 2: Implement QueryDatabaseTool.java**

```java
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
    public String getName() { return "queryDatabase"; }

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
            var response = databaseAnalyzeAgent.analyze(new DatabaseAgentRequest(question));
            return ToolExecutionResult.ok(response.getAnalysis());
        } catch (Exception e) {
            return ToolExecutionResult.fail(e.getMessage());
        }
    }

    @Override
    public boolean isReadOnly() { return true; }
}
```

- [ ] **Step 3: Add starter-mcp to starter-rag/pom.xml**

```xml
        <dependency>
            <groupId>com.aistarter</groupId>
            <artifactId>starter-mcp</artifactId>
        </dependency>
```

- [ ] **Step 4: Write SearchKnowledgeToolTest (mock VectorStore)**

Test: when VectorStore returns one Document, tool result contains snippet text.

- [ ] **Step 5: Implement SearchKnowledgeTool.java**

Reuse retrieval logic from `RagChatService` (similaritySearch with `RagDocument.DEFAULT_COLLECTION`), format top chunks as numbered text for LLM consumption. Parameters: `query` (required), `topK` (optional, default 3).

- [ ] **Step 6: Run tests**

```powershell
mvn -pl starter-agent,starter-rag test -Dtest=QueryDatabaseToolTest,SearchKnowledgeToolTest -q
```

- [ ] **Step 7: Commit**

```powershell
git commit -m "feat(tools): add QueryDatabaseTool and SearchKnowledgeTool"
```

---

## Task 4: SseEvent + StreamToolChatOrchestrator

**Files:**
- Modify: `starter-ai/pom.xml`
- Create: `starter-ai/src/main/java/com/aistarter/ai/stream/SseEvent.java`
- Create: `starter-ai/src/main/java/com/aistarter/ai/stream/StreamToolChatOrchestrator.java`
- Create: `starter-ai/src/test/java/com/aistarter/ai/stream/SseEventTest.java`
- Create: `starter-ai/src/test/java/com/aistarter/ai/stream/StreamToolChatOrchestratorTest.java`

- [ ] **Step 1: Add dependencies to starter-ai/pom.xml**

```xml
        <dependency>
            <groupId>com.aistarter</groupId>
            <artifactId>starter-mcp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
```

- [ ] **Step 2: Create SseEvent.java**

Record with factory methods: `start(conversationId)`, `chunk(content)`, `toolCall(id, name, args)`, `toolResult(id, name, result, success)`, `done(durationMs, metadata)`, `error(code, message, toolName)`. Method `toJson()` using Jackson `ObjectMapper` (inject or static shared mapper).

- [ ] **Step 3: Create StreamToolChatOrchestrator.java**

Core responsibilities:
1. Accept: `conversationId`, `ChatClient.Builder`, system prompt, user prompt, `enableTools`, optional `doneMetadata` supplier
2. Emit `start` event
3. Build `ChatClient` with `.defaultToolCallbacks(ToolCallbackFactory.from(tools))` when enabled
4. Call `.stream().chatResponse()` → `Flux<ChatResponse>`
5. For each response chunk:
   - Extract text delta → `chunk` events
   - Detect tool calls → emit `tool_call`, run `ToolExecutor`, emit `tool_result`, append tool message to conversation, **recurse** for next LLM turn (max 5 tool rounds)
6. Accumulate full assistant text; on complete emit `done` with `durationMs`
7. On exception emit `error` then complete flux
8. Return `Flux<SseEvent>`

**ToolCallbackFactory** (in `starter-mcp`): convert each `Tool` to Spring AI `ToolCallback` using `FunctionToolCallback.builder(name, args -> executor.execute(name, args)).description(...).inputSchema(json).build()` — wire `ToolExecutor` for actual execution so Spring AI and manual SSE events stay consistent.

- [ ] **Step 4: Write StreamToolChatOrchestratorTest**

Mock `ChatClient` to return: (a) one chunk stream without tools → expect `start`, `chunk`, `done`; (b) stream with tool call → expect `tool_call` + `tool_result` ordering.

- [ ] **Step 5: Run tests**

```powershell
mvn -pl starter-ai test -Dtest=SseEventTest,StreamToolChatOrchestratorTest -q
```

- [ ] **Step 6: Commit**

```powershell
git commit -m "feat(ai): add SseEvent and StreamToolChatOrchestrator"
```

---

## Task 5: StreamChatService

**Files:**
- Create: `starter-ai/src/main/java/com/aistarter/ai/dto/ChatStreamRequest.java`
- Create: `starter-ai/src/main/java/com/aistarter/ai/service/StreamChatService.java`
- Create: `starter-ai/src/test/java/com/aistarter/ai/service/StreamChatServiceTest.java`

- [ ] **Step 1: Create ChatStreamRequest.java**

```java
package com.aistarter.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatStreamRequest {
    @NotBlank
    private String message;
    private String sessionId = "default";
    private boolean enableTools = true;
}
```

- [ ] **Step 2: Implement StreamChatService.java**

Mirror `ChatService.chat()` prompt assembly (Redis history + optional system prompt from `PromptService`), then delegate to `StreamToolChatOrchestrator`. On successful `done`, write user message + full assistant reply to `ChatMemory`. On client cancel / error before `done`, **do not** write assistant partial to memory (per spec).

- [ ] **Step 3: Unit test with mocked ChatMemory, PromptService, orchestrator**

- [ ] **Step 4: Commit**

```powershell
git commit -m "feat(ai): add StreamChatService for SSE chat"
```

---

## Task 6: StreamRagChatService

**Files:**
- Create: `starter-rag/src/main/java/com/aistarter/rag/service/StreamRagChatService.java`
- Create: `starter-rag/src/test/java/com/aistarter/rag/service/StreamRagChatServiceTest.java`

- [ ] **Step 1: Implement StreamRagChatService**

Copy retrieval + prompt rendering from `RagChatService.chat()`, call orchestrator with `enableTools` from request (extend `RagChatRequest` usage or accept same fields + `enableTools` default true). Pass `sources` into `done` metadata:

```json
{"metadata":{"sources":[{"documentId":1,"filename":"refund-policy.md","snippet":"..."}]}}
```

- [ ] **Step 2: Unit test — mock VectorStore + orchestrator, assert sources in done metadata**

- [ ] **Step 3: Commit**

```powershell
git commit -m "feat(rag): add StreamRagChatService with SSE and sources metadata"
```

---

## Task 7: REST controllers + Security

**Files:**
- Modify: `starter-web/src/main/java/com/aistarter/web/controller/ApiController.java`
- Modify: `starter-web/src/main/java/com/aistarter/web/controller/RagController.java`
- Modify: `starter-auth/src/main/java/com/aistarter/auth/security/SecurityConfig.java`
- Modify: `starter-web/src/test/java/com/aistarter/web/controller/ApiControllerTest.java`
- Create: `starter-web/src/test/java/com/aistarter/web/controller/RagControllerStreamTest.java`

- [ ] **Step 1: Update ApiController**

```java
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI 聊天（SSE 流式 + Tool Calling）")
    public Flux<ServerSentEvent<String>> chatStream(@Valid @RequestBody ChatStreamRequest request) {
        return streamChatService.chatStream(request)
                .map(ev -> ServerSentEvent.builder(ev.toJson()).build());
    }

    @GetMapping("/tools")
    @Operation(summary = "Tool 列表")
    public List<ToolInfo> listTools() {
        return toolRegistryService.listTools();
    }
```

Inject `StreamChatService`. Change return type of `/tools` from `List<String>` to `List<ToolInfo>`.

- [ ] **Step 2: Update RagController**

Add `/chat/stream` endpoint mirroring chat stream, using `StreamRagChatService`.

- [ ] **Step 3: SecurityConfig — add permitAll**

```java
.requestMatchers(HttpMethod.POST, AppConstants.API_PREFIX + "/chat/stream").permitAll()
```

- [ ] **Step 4: ApiControllerTest — update tools test + add stream test**

Mock `StreamChatService.chatStream()` to return `Flux.just(SseEvent.chunk("hi"), SseEvent.done(100, Map.of()))`. Use `mockMvc.perform(...).andExpect(status().isOk())` — for SSE in MockMvc, consume response content and assert contains `"type":"chunk"`.

Update tools test:

```java
when(toolRegistryService.listTools()).thenReturn(List.of(new ToolInfo("calculateArea", "...", true)));
```

- [ ] **Step 5: Run starter-web tests**

```powershell
mvn -pl starter-web test -q
```

- [ ] **Step 6: Commit**

```powershell
git commit -m "feat(web): add SSE stream endpoints and upgrade /api/tools"
```

---

## Task 8: Full test suite + verify.ps1

**Files:**
- Modify: `scripts/verify.ps1`
- Modify: `examples/api-examples.http`

- [ ] **Step 1: Update verify.ps1 GET /api/tools check**

Assert `$r.Count -ge 3` and names contain `calculateArea`.

- [ ] **Step 2: Add SSE helper function in verify.ps1**

```powershell
function Read-SseEvents {
    param([string]$Url, [byte[]]$Body)
    # Use Invoke-WebRequest -Headers @{Accept='text/event-stream'} ...
    # Parse lines starting with "data: " → return array of parsed JSON objects
}
```

- [ ] **Step 3: Add stream tests in `-SkipAi` block**

1. `POST /api/chat/stream` greeting → events contain `chunk` and `done`
2. `POST /api/chat/stream` with `"message":"Calculate area of rectangle 3 by 4 meters"` → events contain `tool_call` with `calculateArea`
3. `POST /api/rag/chat/stream` (after RAG upload block or reuse refund policy) → `chunk` + `done` with `metadata.sources`

- [ ] **Step 4: Run full Maven test suite**

```powershell
cd d:\py\pysasuke
mvn test -q
```

Expected: exit 0

- [ ] **Step 5: Commit**

```powershell
git commit -m "test: add verify.ps1 SSE stream and tool smoke tests"
```

---

## Task 9: Documentation

**Files:**
- Modify: `README.md`
- Create: `examples/stream-chat-examples.http`

- [ ] **Step 1: README — add section after Agent Router**

Content:
- Streaming Chat (`POST /api/chat/stream`)
- Streaming RAG (`POST /api/rag/chat/stream`)
- Tool Calling overview + `/api/tools`
- curl example reading SSE (or note use browser EventSource / `.http` file)
- Mention v0.8 Admin UI deferred

- [ ] **Step 2: Create examples/stream-chat-examples.http**

Include:
- POST `/api/chat/stream` with JSON body
- POST `/api/rag/chat/stream`
- GET `/api/tools`
- Comment showing expected event sequence

- [ ] **Step 3: Update examples/api-examples.http tools section for new response shape**

- [ ] **Step 4: Commit**

```powershell
git commit -m "docs: add streaming chat and tool calling examples"
```

---

## Task 10: Final verification

- [ ] **Step 1: Full build**

```powershell
mvn clean test -q
```

- [ ] **Step 2: Manual smoke (optional, if .env available)**

```powershell
docker compose up -d postgres redis
mvn install -DskipTests
# load .env, start jar, then:
.\scripts\verify.ps1
```

- [ ] **Step 3: Ready for @git-ship-feature v0.7**

Branch: `feature/v0.7-streaming-tools`  
Release title: `v0.7.0 - Streaming Chat + Tool Calling`

---

## Plan self-review

| Spec requirement | Task |
|------------------|------|
| POST /api/chat/stream | Task 7 |
| POST /api/rag/chat/stream | Task 7 |
| SSE JSON events (6 types) | Task 4 |
| 3 Tools | Tasks 1–3 |
| /api/tools metadata | Task 2, 7 |
| enableTools flag | Tasks 5–6 |
| RAG sources in done.metadata | Task 6 |
| Security permitAll chat/stream | Task 7 |
| No partial memory on disconnect | Task 5 |
| verify.ps1 4 new checks | Task 8 |
| README + examples | Task 9 |
| Old APIs unchanged | Tasks 5–7 (new endpoints only) |

**Placeholder scan:** No TBD/TODO.  
**Type consistency:** `ToolInfo`, `SseEvent`, `ChatStreamRequest` names match across tasks.  
**Cycle check:** Tools split across modules per File Map dependency rule.
