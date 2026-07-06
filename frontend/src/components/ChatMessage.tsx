interface ChatMessageProps {
  role: 'user' | 'assistant';
  content: string;
  streaming?: boolean;
}

export default function ChatMessage({ role, content, streaming }: ChatMessageProps) {
  if (role === 'user') {
    return (
      <div className="flex justify-end mb-4">
        <div className="max-w-[70%] bg-elevated border border-border-base rounded-lg px-4 py-2.5 text-sm font-body text-text-primary">
          {content}
        </div>
      </div>
    );
  }

  return (
    <div className="flex mb-4">
      <div className="border-l-2 border-cyan pl-4 max-w-[85%]">
        <p
          className={`text-sm font-body text-text-primary whitespace-pre-wrap ${
            streaming ? 'streaming-cursor' : ''
          }`}
        >
          {content}
        </p>
      </div>
    </div>
  );
}
