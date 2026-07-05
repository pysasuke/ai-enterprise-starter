# AI Enterprise Admin UI

React + TypeScript 管理控制台，构建时嵌入 Spring Boot jar。

## Tech Stack

- React 18 + TypeScript 5
- Vite 5 + TailwindCSS 3
- React Router 6
- `@microsoft/fetch-event-source`（SSE）
- lucide-react + framer-motion

## Development

```bash
npm install
npm run dev
```

Dev server: http://localhost:5173  
API proxy: `/api/*` → http://localhost:8080

Requires backend running (`java -jar starter-demo/target/starter-demo-0.1.0-SNAPSHOT.jar` or `mvn -pl starter-demo spring-boot:run`).

## Build

```bash
npm run build   # outputs to dist/
```

Maven integration (root `pom.xml`):

```bash
mvn clean package -DskipTests   # auto npm install + build + copy to jar
```

## Pages

| Route | Module |
|-------|--------|
| `/login` | JWT login |
| `/chat` | Streaming chat + tool calls |
| `/rag` | Document management + RAG chat |
| `/prompts` | Prompt version editor |
| `/tools` | Tool overview |
