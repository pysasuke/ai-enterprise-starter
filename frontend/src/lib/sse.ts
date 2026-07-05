import { fetchEventSource } from '@microsoft/fetch-event-source';
import { auth } from './auth';

export interface SseEvent {
  type: 'start' | 'chunk' | 'tool_call' | 'tool_result' | 'done' | 'error';
  content?: string;
  toolId?: string;
  toolName?: string;
  arguments?: Record<string, unknown>;
  success?: boolean;
  result?: string;
  message?: string;
  metadata?: {
    sources?: Array<{ documentId: number | null; filename: string; snippet: string }>;
  };
}

interface StreamOptions {
  path: string;
  body: Record<string, unknown>;
  onEvent: (event: SseEvent) => void;
  onError: (error: Error) => void;
}

export async function streamSse({ path, body, onEvent, onError }: StreamOptions): Promise<void> {
  const token = auth.getToken();
  await fetchEventSource(`/api${path}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(body),
    onmessage(ev) {
      try {
        const data = JSON.parse(ev.data) as SseEvent;
        onEvent(data);
      } catch {
        // ignore malformed events
      }
    },
    onerror(err) {
      onError(err instanceof Error ? err : new Error('SSE error'));
      throw err;
    },
  });
}

export const streamChat = (
  body: { message: string; enableTools?: boolean },
  onEvent: (e: SseEvent) => void,
  onError: (e: Error) => void,
) => streamSse({ path: '/chat/stream', body, onEvent, onError });

export const streamRagChat = (
  body: { question: string; topK?: number; enableTools?: boolean },
  onEvent: (e: SseEvent) => void,
  onError: (e: Error) => void,
) => streamSse({ path: '/rag/chat/stream', body, onEvent, onError });
