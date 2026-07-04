# V2 Streaming Chat + Tool Calling 设计（v0.7.0）

**日期：** 2026-07-04  
**状态：** 已批准  
**版本目标：** v0.7.0 - Streaming Chat + Tool Calling

---

## 1. 背景

V2 已完成 RAG（v0.2）、Prompt（v0.3）、OCR（v0.4）、Workflow（v0.5）、Agent Router（v0.6）。

当前 Chat / RAG 均为阻塞式 JSON 响应，用户需等待完整生成；`starter-mcp` 的 `/api/tools` 为占位列表，未真正启用 Tool Calling。

下一项为 **v0.7.0**：**SSE 流式响应 + Tool Calling 完整集成**，让 Chat / RAG 实时输出，并支持 LLM 自动调用数据库查询、知识库检索、几何面积计算等 Tool。

**已确认决策（brainstorming）：**

| 项 | 选择 |
|----|------|
| 流式端点策略 | 新增 `/stream` 专用端点；原 API 不变 |
| 流式协议 | SSE（Server-Sent Events） |
| 事件格式 | JSON 事件流（`type` + 结构化 payload） |
| Tool 集成 | 方案 B — 流式 + Tool 完整集成（同一 SSE 流推送 tool_call / tool_result） |
| Tool 清单 | `queryDatabase`、`searchKnowledge`、`calculateArea`（3 个，均为只读） |
| Tool 执行 | 只读 Tool 自动执行，无需用户确认 |
| Admin UI | 推迟至 v0.8.0 |

---

## 2. 目标

- 新增 `POST /api/chat/stream`、`POST /api/rag/chat/stream`，SSE 实时推送生成内容
- 流式过程中支持多轮 Tool Calling，事件可观测（tool_call → tool_result → 继续 chunk）
- 实现 3 个实用 Tool，升级 `/api/tools` 返回真实元数据
- 复用现有 `ChatService` 逻辑（Prompt、Redis Memory）、`RagChatService` 检索链路、`DatabaseAnalyzeAgent`
- 不破坏现有非流式 API 与 `verify.ps1` 已有验收项

---

## 3. 非目标（v0.7 不做）

- Admin UI / Web 管理后台（v0.8.0）
- Workflow / Agent Router 流式化
- MCP Client 连接外部 MCP Server
- WebSocket 双向通信 / 中途取消 API
- 限流、配额、Token 成本统计
- 写操作 Tool（发邮件、执行代码等）及用户确认流程
- 改造 `/api/chat` 或 `/api/rag/chat` 为流式（仅新增 `/stream` 端点）

---

## 4. 架构

### 4.1 整体结构

```
客户端
  EventSource / fetch(SSE) → 解析 JSON 事件
        │
        ▼
starter-web
  ApiController      POST /api/chat/stream
  RagController      POST /api/rag/chat/stream
        │
        ▼
starter-ai                    starter-rag
  StreamChatService             StreamRagChatService
  SseEventEmitter（共用）       （RAG 检索 + 流式生成 + Tool）
        │
        ├─ ChatClient.stream() + Function Calling
        │
        ▼
starter-mcp
  ToolRegistryService（升级）
  ToolExecutor
  tools/
    QueryDatabaseTool      → DatabaseAnalyzeAgent
    SearchKnowledgeTool    → VectorStore / Rag 检索
    CalculateAreaTool      → 本地几何计算
```

### 4.2 模块依赖

```
starter-mcp
  → starter-common
  → starter-agent   (QueryDatabaseTool)
  → starter-rag     (SearchKnowledgeTool)

starter-ai
  → starter-mcp     (ToolRegistry, ToolExecutor)
  → 现有 starter-ai 依赖不变

starter-rag
  → starter-mcp     (StreamRagChatService 可选 Tool)
  → 现有 starter-rag 依赖不变

starter-web
  → starter-ai, starter-rag（Controller 层）
```

### 4.3 核心组件

| 组件 | 模块 | 职责 |
|------|------|------|
| `StreamChatService` | starter-ai | Chat 流式编排：LLM stream + Tool 多轮 + SSE 事件 |
| `StreamRagChatService` | starter-rag | 向量检索 → Prompt 渲染 → 流式生成 + Tool |
| `SseEvent` / `SseEventEmitter` | starter-ai | 统一 JSON 事件构造与序列化 |
| `Tool` 接口 | starter-mcp | Tool 契约：name、description、schema、execute、isReadOnly |
| `ToolRegistryService` | starter-mcp | 注册 3 个 Tool，供 LLM 与 `/api/tools` 使用 |
| `ToolExecutor` | starter-mcp | 按 name 分发执行，捕获异常返回 `ToolExecutionResult` |

---

## 5. SSE 事件协议

### 5.1 事件类型

| type | 说明 |
|------|------|
| `start` | 流开始 |
| `chunk` | 文本片段 |
| `tool_call` | LLM 决定调用 Tool |
| `tool_result` | Tool 执行结果 |
| `done` | 流结束（含 token / 耗时元数据） |
| `error` | 不可恢复错误或流中断 |

### 5.2 事件格式

**start**
```json
{"type":"start","conversationId":"uuid-123","timestamp":1720095600000}
```

**chunk**
```json
{"type":"chunk","content":"让我帮你计算一下"}
```

**tool_call**
```json
{"type":"tool_call","toolId":"call_abc123","toolName":"calculateArea","arguments":{"shape":"rectangle","width":3,"height":4}}
```

**tool_result**
```json
{"type":"tool_result","toolId":"call_abc123","toolName":"calculateArea","result":"12.00","success":true}
```

**done**
```json
{"type":"done","totalTokens":150,"promptTokens":100,"completionTokens":50,"finishReason":"stop","durationMs":2500}
```

**error**
```json
{"type":"error","code":"TOOL_EXECUTION_FAILED","message":"数据库连接失败","toolName":"queryDatabase"}
```

SSE 行格式：`data: {json}\n\n`。流结束时发送 `done` 事件后关闭连接；不再额外发送裸 `[DONE]` 字符串。

### 5.3 多轮 Tool Calling 流程

```
用户消息
  → start
  → chunk（LLM 思考文本）
  → tool_call（queryDatabase）
  → tool_result
  → chunk（LLM 基于 Tool 结果继续）
  → tool_call（calculateArea）   // 若 LLM 决定继续调用
  → tool_result
  → chunk（最终回答）
  → done
```

---

## 6. Tool 设计

### 6.1 Tool 接口

```java
public interface Tool {
    String getName();
    String getDescription();
    Map<String, Object> getParametersSchema();  // JSON Schema
    ToolExecutionResult execute(Map<String, Object> arguments);
    boolean isReadOnly();
}

public record ToolExecutionResult(boolean success, String result, String error) {}
```

### 6.2 Tool 清单

| name | 说明 | 依赖 | readOnly |
|------|------|------|----------|
| `queryDatabase` | 自然语言数据库 Schema 分析与 SQL 优化建议 | `DatabaseAnalyzeAgent` | true |
| `searchKnowledge` | 检索知识库，返回相关片段 | `VectorStore` + RAG 元数据 | true |
| `calculateArea` | 几何面积：rectangle / triangle / circle / trapezoid | 本地计算 | true |

### 6.3 calculateArea 参数

| shape | 必填参数 | 公式 |
|-------|----------|------|
| `rectangle` | width, height | w × h |
| `triangle` | width（底）, height | w × h ÷ 2 |
| `circle` | radius | π × r² |
| `trapezoid` | topBase, bottomBase, height | (上底 + 下底) × 高 ÷ 2 |

返回值为保留两位小数的字符串（如 `"12.00"`）。

### 6.4 `/api/tools` 响应升级

由占位字符串列表改为 Tool 元数据数组：

```json
[
  {"name":"queryDatabase","description":"...","readOnly":true},
  {"name":"searchKnowledge","description":"...","readOnly":true},
  {"name":"calculateArea","description":"...","readOnly":true}
]
```

---

## 7. API

### 7.1 新接口

**Chat 流式**
```
POST /api/chat/stream
Content-Type: application/json
Accept: text/event-stream
```

```json
{
  "message": "帮我查 orders 表并计算 3×4 面积",
  "sessionId": "demo",
  "enableTools": true
}
```

| 字段 | 必填 | 默认 | 说明 |
|------|------|------|------|
| `message` | 是 | — | 用户消息 |
| `sessionId` | 否 | `"default"` | Redis 会话 ID |
| `enableTools` | 否 | `true` | `false` 时不注册 Tool，仅流式文本 |

**RAG 流式**
```
POST /api/rag/chat/stream
Content-Type: application/json
Accept: text/event-stream
```

```json
{
  "question": "退款政策是什么？",
  "topK": 5,
  "enableTools": true
}
```

| 字段 | 必填 | 默认 | 说明 |
|------|------|------|------|
| `question` | 是 | — | 用户问题 |
| `topK` | 否 | 5 | 检索条数 |
| `enableTools` | 否 | `true` | 同上 |

响应：`Content-Type: text/event-stream`，HTTP 200。

RAG 流式可在 `done` 事件或单独 `metadata` 事件中附带 `sources[]`（与现有 `RagSource` 结构一致）；实现时二选一，优先在 `done` 的 `metadata.sources` 字段返回。

### 7.2 旧接口（不变）

```
POST /api/chat
POST /api/rag/chat
GET  /api/tools          （响应格式升级，路径不变）
POST /api/agent/database
POST /api/workflows/**
```

---

## 8. 流式服务逻辑

### 8.1 StreamChatService

1. 发送 `start` 事件
2. 加载 Redis 会话历史 + Prompt（与 `ChatService` 一致）
3. 若 `enableTools=true`，向 ChatClient 注册 3 个 Tool（Spring AI Function Calling）
4. 订阅 `ChatClient.stream()`：
   - 文本 delta → `chunk` 事件
   - Tool call → `tool_call` → `ToolExecutor` → `tool_result` → 将结果追加上下文，继续生成
5. 流结束后写入 Redis Memory（user + assistant 完整内容）
6. 发送 `done` 事件

### 8.2 StreamRagChatService

1. 执行向量检索（与 `RagChatService.chat` 相同）
2. 渲染 RAG Prompt
3. 流式生成 + 可选 Tool Calling（逻辑同 StreamChatService）
4. `done` 时附带 `sources`

### 8.3 客户端断开

取消 `Flux` 订阅，停止 LLM 流，不写入不完整 assistant 消息到 Memory（或写入 partial，实现时选「不写入」以保持 Memory 一致性）。

---

## 9. 错误处理

| 场景 | 行为 |
|------|------|
| LLM 流中断 / 超时 | 发送 `error` 事件，关闭 SSE |
| Tool 参数非法 | `tool_result.success=false`，LLM 继续解释 |
| Tool 执行异常 | 同上，不中断整个流 |
| `enableTools=false` 但 LLM 尝试调用 | 忽略 Tool 调用，仅输出 chunk |
| 客户端断开 | 取消订阅，停止 LLM |
| `message` / `question` 为空 | HTTP 400（`@NotBlank`） |

原则：流式场景尽量部分成功；已推送的 chunk 与 tool_result 保留。

---

## 10. 安全

- `SecurityConfig` 新增：`POST /api/chat/stream` → `permitAll`
- `/api/rag/chat/stream` 已在 `/api/rag/**` 规则内
- 3 个 Tool 均为只读，v0.7 不做执行确认
- v0.7 不做限流

---

## 11. 测试

| 类型 | 覆盖 |
|------|------|
| 单元 | `CalculateAreaTool`：4 种图形 + 缺参 |
| 单元 | `QueryDatabaseTool`、`SearchKnowledgeTool`（Mock 依赖） |
| 单元 | `ToolRegistryService`：3 个 Tool 元数据 |
| 单元 | `StreamChatService`：chunk 序列 + tool_call/result 顺序 |
| 单元 | `StreamRagChatService`：检索 + 流式 + sources |
| Web | `ApiControllerTest`：`/chat/stream` SSE 事件 |
| Web | `RagControllerTest`：`/rag/chat/stream` SSE 事件 |
| 回归 | 原 `/api/chat`、`/api/rag/chat`、v0.5/v0.6 workflow 测试不受影响 |

**verify.ps1 新增（AI 块内）：**

1. `POST /api/chat/stream` — 普通问候 → `chunk` + `done`
2. `POST /api/chat/stream` — 面积计算 → `tool_call` + `tool_result`
3. `GET /api/tools` — 返回 3 个 Tool
4. `POST /api/rag/chat/stream` — 知识库问题 → `chunk` + `done`（需预上传文档）

---

## 12. 文档与发版

- `README.md`：Streaming Chat + Tool Calling 小节
- `examples/stream-chat-examples.http`
- `examples/api-examples.http`：补充 `/tools` 新格式
- `scripts/verify.ps1`：新增 stream 验收项
- `docs/internal/RELEASE-v0.7.0.md`（发版时新建）

**分支建议：** `feature/v0.7-streaming-tools`  
**发版：** v0.7.0 - Streaming Chat + Tool Calling

**预估工作量：** 7–8 天

---

## 13. 实现顺序（spec 批准后 → writing-plans）

1. `starter-mcp`：Tool 接口 + 3 个实现 + ToolExecutor + 升级 ToolRegistryService
2. TDD：CalculateAreaTool、QueryDatabaseTool、SearchKnowledgeTool
3. `starter-ai`：SseEvent + StreamChatService
4. `starter-rag`：StreamRagChatService
5. `starter-web`：Controller 端点 + SecurityConfig
6. 测试 + verify.ps1 + README/examples

---

## 14. 后续扩展（不在 v0.7）

- v0.8.0 Admin UI（Prompt / 知识库 / 日志可视化）
- Workflow / Agent Router 流式化
- MCP Client 外部 Tool Server
- 限流、Token 成本统计
- 写操作 Tool + 用户确认流程

---

## Spec self-review

| 检查 | 结果 |
|------|------|
| TBD / TODO | 无 |
| 内部矛盾 | 无；新 `/stream` 与旧 API 边界清晰 |
| 范围 | 扩展 starter-mcp/ai/rag + 2 个端点，适合一个 implementation plan |
| 歧义 | RAG sources 在 `done.metadata.sources` 返回；客户端断开不写入 partial Memory |
| Tool 数量 | 3 个（无 getCurrentTime），与 brainstorming 最终确认一致 |
| 协议 | SSE + JSON 事件；结束用 `done` 事件，不用裸 `[DONE]` |
