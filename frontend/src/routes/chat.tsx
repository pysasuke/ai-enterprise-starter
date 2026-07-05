import { useState, useRef, FormEvent } from 'react';
import { Send } from 'lucide-react';
import ChatMessage from '../components/ChatMessage';
import ToolCallCard from '../components/ToolCallCard';
import { streamChat, SseEvent } from '../lib/sse';
import { useToast } from '../context/ToastContext';

interface Message {
  role: 'user' | 'assistant';
  content: string;
  streaming?: boolean;
}

interface ToolCall {
  id: string;
  toolName: string;
  arguments: Record<string, unknown>;
  result?: string;
  success?: boolean;
  pending?: boolean;
}

export default function Chat() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [tools, setTools] = useState<ToolCall[]>([]);
  const [input, setInput] = useState('');
  const [enableTools, setEnableTools] = useState(true);
  const [streaming, setStreaming] = useState(false);
  const { show } = useToast();
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!input.trim() || streaming) return;

    const userMsg = input;
    setMessages((m) => [
      ...m,
      { role: 'user', content: userMsg },
      { role: 'assistant', content: '', streaming: true },
    ]);
    setTools([]);
    setInput('');
    setStreaming(true);

    const onEvent = (ev: SseEvent) => {
      if (ev.type === 'chunk' && ev.content) {
        setMessages((m) => {
          const last = m[m.length - 1];
          if (last?.role === 'assistant') {
            return [...m.slice(0, -1), { ...last, content: last.content + ev.content, streaming: true }];
          }
          return m;
        });
      } else if (ev.type === 'tool_call' && ev.toolId && ev.toolName) {
        setTools((t) => [
          ...t,
          {
            id: ev.toolId!,
            toolName: ev.toolName!,
            arguments: ev.arguments || {},
            pending: true,
          },
        ]);
      } else if (ev.type === 'tool_result' && ev.toolId) {
        setTools((t) =>
          t.map((x) =>
            x.id === ev.toolId
              ? { ...x, result: ev.result, success: ev.success, pending: false }
              : x,
          ),
        );
      } else if (ev.type === 'done') {
        setMessages((m) => {
          const last = m[m.length - 1];
          if (last?.role === 'assistant') {
            return [...m.slice(0, -1), { ...last, streaming: false }];
          }
          return m;
        });
      } else if (ev.type === 'error') {
        show('error', ev.message || 'Stream error');
      }
    };

    const onError = (err: Error) => {
      show('error', err.message);
      setMessages((m) => {
        const last = m[m.length - 1];
        if (last?.role === 'assistant') {
          return [...m.slice(0, -1), { ...last, streaming: false }];
        }
        return m;
      });
    };

    try {
      await streamChat({ message: userMsg, enableTools }, onEvent, onError);
    } finally {
      setStreaming(false);
      setTimeout(() => messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' }), 100);
    }
  };

  return (
    <div className="flex-1 flex flex-col max-w-4xl mx-auto w-full px-6 py-6">
      <div className="flex-1 overflow-y-auto pb-4">
        {messages.length === 0 && (
          <div className="flex items-center justify-center h-full">
            <p className="font-display text-xl text-text-muted">向 AI 提问 · 自动调用 Tool</p>
          </div>
        )}
        {messages.map((msg, i) => (
          <div key={i}>
            <ChatMessage role={msg.role} content={msg.content} streaming={msg.streaming} />
            {msg.role === 'assistant' && tools.length > 0 && i === messages.length - 1 && (
              <div className="ml-6 mb-2">
                {tools.map((t) => (
                  <ToolCallCard key={t.id} {...t} />
                ))}
              </div>
            )}
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
      <form onSubmit={handleSubmit} className="border-t border-border-base pt-4 flex items-center gap-3">
        <label className="flex items-center gap-2 text-xs font-body text-text-secondary cursor-pointer">
          <input
            type="checkbox"
            checked={enableTools}
            onChange={(e) => setEnableTools(e.target.checked)}
            className="accent-cyan"
          />
          Enable Tools
        </label>
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          disabled={streaming}
          placeholder="输入消息..."
          className="flex-1 bg-elevated border border-border-base rounded px-3 py-2 text-sm font-body text-text-primary focus:border-cyan focus:outline-none"
        />
        <button
          type="submit"
          disabled={streaming || !input.trim()}
          className="bg-cyan text-base p-2 rounded hover:shadow-[0_0_15px_rgba(0,212,255,0.4)] transition-shadow disabled:opacity-30"
        >
          <Send className="w-4 h-4" />
        </button>
      </form>
    </div>
  );
}
