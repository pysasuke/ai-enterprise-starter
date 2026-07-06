import { useState } from 'react';
import { Wrench, ChevronDown, ChevronRight, Check, X } from 'lucide-react';

interface ToolCallCardProps {
  toolName: string;
  arguments: Record<string, unknown>;
  result?: string;
  success?: boolean;
  pending?: boolean;
}

export default function ToolCallCard({
  toolName,
  arguments: args,
  result,
  success,
  pending,
}: ToolCallCardProps) {
  const [expanded, setExpanded] = useState(false);

  return (
    <div
      className={`border rounded-lg my-2 overflow-hidden ${
        pending
          ? 'border-magenta/50 bg-magenta/5 animate-pulse'
          : success
            ? 'border-green/40 bg-green/5'
            : 'border-magenta/40 bg-magenta/5'
      }`}
    >
      <button
        onClick={() => setExpanded((v) => !v)}
        className="w-full flex items-center gap-2 px-3 py-2 text-left"
      >
        {expanded ? (
          <ChevronDown className="w-3.5 h-3.5 text-magenta" />
        ) : (
          <ChevronRight className="w-3.5 h-3.5 text-magenta" />
        )}
        <Wrench className="w-3.5 h-3.5 text-magenta" />
        <span className="text-xs font-body text-magenta font-medium">{toolName}</span>
        <span className="text-xs font-body text-text-secondary truncate">
          (
          {Object.entries(args)
            .map(([k, v]) => `${k}: ${String(v)}`)
            .join(', ')}
          )
        </span>
        {success === true && <Check className="w-3.5 h-3.5 text-green ml-auto" />}
        {success === false && <X className="w-3.5 h-3.5 text-magenta ml-auto" />}
      </button>
      {expanded && (
        <div className="px-3 pb-3 text-xs font-body space-y-1.5 border-t border-border-base/50 pt-2">
          <div>
            <span className="text-text-secondary">arguments:</span>
            <pre className="text-text-primary mt-1 whitespace-pre-wrap">{JSON.stringify(args, null, 2)}</pre>
          </div>
          {result !== undefined && (
            <div>
              <span className="text-text-secondary">result:</span>
              <pre className={`mt-1 whitespace-pre-wrap ${success ? 'text-green' : 'text-magenta'}`}>
                {result}
              </pre>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
