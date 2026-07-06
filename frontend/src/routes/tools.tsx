import { useEffect, useState } from 'react';
import { Wrench, BookOpen, Ruler, ShieldCheck } from 'lucide-react';
import { api } from '../lib/api';
import { useToast } from '../context/ToastContext';

interface ToolInfo {
  name: string;
  description: string;
  readOnly: boolean;
}

const TOOL_ICONS: Record<string, typeof Wrench> = {
  queryDatabase: Wrench,
  searchKnowledge: BookOpen,
  calculateArea: Ruler,
};

const TOOL_STYLES: Record<string, { border: string; bg: string; text: string; iconBg: string }> = {
  queryDatabase: {
    border: 'border-magenta/30',
    bg: 'bg-panel',
    text: 'text-magenta',
    iconBg: 'bg-magenta/10',
  },
  searchKnowledge: {
    border: 'border-green/30',
    bg: 'bg-panel',
    text: 'text-green',
    iconBg: 'bg-green/10',
  },
  calculateArea: {
    border: 'border-cyan/30',
    bg: 'bg-panel',
    text: 'text-cyan',
    iconBg: 'bg-cyan/10',
  },
};

const DEFAULT_STYLE = {
  border: 'border-cyan/30',
  bg: 'bg-panel',
  text: 'text-cyan',
  iconBg: 'bg-cyan/10',
};

export default function Tools() {
  const [tools, setTools] = useState<ToolInfo[]>([]);
  const { show } = useToast();

  useEffect(() => {
    api.tools
      .list()
      .then(setTools)
      .catch((e: unknown) => show('error', e instanceof Error ? e.message : '加载失败'));
  }, []);

  return (
    <div className="flex-1 max-w-4xl mx-auto w-full px-6 py-8">
      <h1 className="font-display text-2xl text-text-primary mb-1">Available Tools</h1>
      <p className="text-sm text-text-secondary mb-6 font-body">LLM 可自动调用的只读工具</p>
      <div className="grid gap-4">
        {tools.map((tool) => {
          const Icon = TOOL_ICONS[tool.name] || Wrench;
          const style = TOOL_STYLES[tool.name] || DEFAULT_STYLE;
          return (
            <div key={tool.name} className={`${style.bg} border ${style.border} rounded-lg p-5 glow-hover`}>
              <div className="flex items-start gap-3">
                <div className={`p-2 rounded ${style.iconBg}`}>
                  <Icon className={`w-5 h-5 ${style.text}`} />
                </div>
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <h3 className={`font-display text-base ${style.text}`}>{tool.name}</h3>
                    <span
                      className={`flex items-center gap-1 text-xs font-body px-1.5 py-0.5 rounded border ${style.border} ${style.text}`}
                    >
                      <ShieldCheck className="w-3 h-3" /> READ
                    </span>
                  </div>
                  <p className="text-sm text-text-secondary mt-1 font-body">{tool.description}</p>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
