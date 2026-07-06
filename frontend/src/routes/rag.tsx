import { useState, useEffect, useRef, FormEvent } from 'react';
import { Upload, Trash2, Send, FileText } from 'lucide-react';
import { api } from '../lib/api';
import { streamRagChat, SseEvent } from '../lib/sse';
import { useToast } from '../context/ToastContext';
import ChatMessage from '../components/ChatMessage';

interface Doc {
  id: number;
  filename: string;
  contentType: string;
  chunkCount: number;
  status: string;
}

interface RagMessage {
  role: 'user' | 'assistant';
  content: string;
  streaming?: boolean;
  sources?: Array<{ documentId: number | null; filename: string; snippet: string }>;
}

export default function Rag() {
  const [docs, setDocs] = useState<Doc[]>([]);
  const [messages, setMessages] = useState<RagMessage[]>([]);
  const [input, setInput] = useState('');
  const [streaming, setStreaming] = useState(false);
  const { show } = useToast();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const loadDocs = async () => {
    try {
      const list = await api.rag.listDocuments();
      setDocs(list);
    } catch (e: unknown) {
      show('error', e instanceof Error ? e.message : '加载失败');
    }
  };

  useEffect(() => {
    loadDocs();
  }, []);

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      await api.rag.uploadDocument(file);
      show('success', `已上传 ${file.name}`);
      loadDocs();
    } catch (err: unknown) {
      show('error', err instanceof Error ? err.message : '上传失败');
    }
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleDelete = async (id: number) => {
    try {
      await api.rag.deleteDocument(id);
      show('success', '已删除');
      loadDocs();
    } catch (err: unknown) {
      show('error', err instanceof Error ? err.message : '删除失败');
    }
  };

  const handleAsk = async (e: FormEvent) => {
    e.preventDefault();
    if (!input.trim() || streaming) return;

    const q = input;
    setMessages((m) => [
      ...m,
      { role: 'user', content: q },
      { role: 'assistant', content: '', streaming: true },
    ]);
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
      } else if (ev.type === 'done') {
        setMessages((m) => {
          const last = m[m.length - 1];
          if (last?.role === 'assistant') {
            return [...m.slice(0, -1), { ...last, streaming: false, sources: ev.metadata?.sources }];
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
      await streamRagChat({ question: q, enableTools: true }, onEvent, onError);
    } finally {
      setStreaming(false);
      setTimeout(() => messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' }), 100);
    }
  };

  return (
    <div className="flex-1 flex max-w-7xl mx-auto w-full px-6 py-6 gap-6">
      <div className="w-[30%] min-w-[240px] flex flex-col">
        <h2 className="font-display text-sm text-text-secondary uppercase tracking-wider mb-3">
          Knowledge Base
        </h2>
        <button
          onClick={() => fileInputRef.current?.click()}
          className="mb-3 w-full border border-dashed border-border-glow rounded-lg py-3 text-sm font-body text-text-secondary hover:border-cyan hover:text-cyan transition-colors flex items-center justify-center gap-2"
        >
          <Upload className="w-4 h-4" /> Upload Document
        </button>
        <input
          ref={fileInputRef}
          type="file"
          onChange={handleUpload}
          className="hidden"
          accept=".txt,.md,.pdf,.docx"
        />
        <div className="flex-1 overflow-y-auto space-y-2">
          {docs.length === 0 && (
            <p className="text-xs text-text-muted text-center py-8 font-body">Upload first document</p>
          )}
          {docs.map((doc) => (
            <div key={doc.id} className="bg-panel border border-border-base rounded-lg p-3 glow-hover">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-2 min-w-0">
                  <FileText className="w-4 h-4 text-green flex-shrink-0" />
                  <span className="text-xs font-body text-text-primary truncate">{doc.filename}</span>
                </div>
                <button
                  onClick={() => handleDelete(doc.id)}
                  className="text-text-muted hover:text-magenta transition-colors"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
              <p className="text-xs text-text-muted mt-1 font-body">
                {doc.chunkCount} chunks · {doc.status}
              </p>
            </div>
          ))}
        </div>
      </div>
      <div className="flex-1 flex flex-col">
        <div className="flex-1 overflow-y-auto pb-4">
          {messages.length === 0 && (
            <div className="flex items-center justify-center h-full">
              <p className="font-display text-xl text-text-muted">向知识库提问</p>
            </div>
          )}
          {messages.map((msg, i) => (
            <div key={i}>
              <ChatMessage role={msg.role} content={msg.content} streaming={msg.streaming} />
              {msg.role === 'assistant' && msg.sources && msg.sources.length > 0 && (
                <div className="ml-6 mb-3 border border-green/30 bg-green/5 rounded p-2">
                  <p className="text-xs text-green font-body mb-1">Sources:</p>
                  {msg.sources.map((s, j) => (
                    <div key={j} className="text-xs font-body text-text-secondary">
                      <span className="text-green">[{j + 1}]</span> {s.filename}: {s.snippet.slice(0, 80)}...
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
          <div ref={messagesEndRef} />
        </div>
        <form onSubmit={handleAsk} className="border-t border-border-base pt-4 flex items-center gap-3">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            disabled={streaming}
            placeholder="提问..."
            className="flex-1 bg-elevated border border-border-base rounded px-3 py-2 text-sm font-body text-text-primary focus:border-green focus:outline-none"
          />
          <button
            type="submit"
            disabled={streaming || !input.trim()}
            className="bg-green text-base p-2 rounded hover:shadow-[0_0_15px_rgba(0,255,136,0.4)] transition-shadow disabled:opacity-30"
          >
            <Send className="w-4 h-4" />
          </button>
        </form>
      </div>
    </div>
  );
}
