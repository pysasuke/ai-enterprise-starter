import { useState, useEffect } from 'react';
import { api, PromptDefinition, PromptVersion } from '../lib/api';
import { useToast } from '../context/ToastContext';
import { ChevronRight, FileText, Check } from 'lucide-react';

export default function Prompts() {
  const [defs, setDefs] = useState<PromptDefinition[]>([]);
  const [selected, setSelected] = useState<PromptDefinition | null>(null);
  const [versions, setVersions] = useState<PromptVersion[]>([]);
  const [editorContent, setEditorContent] = useState('');
  const [rendered, setRendered] = useState('');
  const { show } = useToast();

  const loadDefs = async () => {
    try {
      const list = await api.prompts.list();
      setDefs(list);
      if (list.length > 0 && !selected) {
        await selectDef(list[0]);
      }
    } catch (e: unknown) {
      show('error', e instanceof Error ? e.message : '加载失败');
    }
  };

  useEffect(() => {
    loadDefs();
  }, []);

  const selectDef = async (def: PromptDefinition) => {
    setSelected(def);
    setRendered('');
    try {
      const v = await api.prompts.getVersions(def.key, def.type);
      setVersions(v);
      const active = v.find((x) => x.active) || v.find((x) => x.version === def.activeVersion);
      setEditorContent(active?.content || '');
    } catch (e: unknown) {
      show('error', e instanceof Error ? e.message : '加载版本失败');
    }
  };

  const handleSave = async () => {
    if (!selected) return;
    try {
      await api.prompts.createVersion(selected.key, selected.type, editorContent);
      show('success', '已创建新版本');
      await loadDefs();
      await selectDef(selected);
    } catch (e: unknown) {
      show('error', e instanceof Error ? e.message : '保存失败');
    }
  };

  const handleActivate = async (version: number) => {
    if (!selected) return;
    try {
      await api.prompts.activate(selected.key, selected.type, version);
      show('success', `v${version} 已激活`);
      await loadDefs();
      await selectDef(selected);
    } catch (e: unknown) {
      show('error', e instanceof Error ? e.message : '激活失败');
    }
  };

  const handleRender = async () => {
    if (!selected) return;
    try {
      const r = await api.prompts.render(selected.key, selected.type, {
        question: '示例问题',
        schema: 'orders',
        indexes: 'idx_id',
        context: '示例上下文',
      });
      setRendered(r.rendered);
    } catch (e: unknown) {
      show('error', e instanceof Error ? e.message : '渲染失败');
    }
  };

  const grouped = defs.reduce<Record<string, PromptDefinition[]>>((acc, d) => {
    (acc[d.key] = acc[d.key] || []).push(d);
    return acc;
  }, {});

  const activeVersion = selected?.activeVersion;

  return (
    <div className="flex-1 flex max-w-7xl mx-auto w-full px-6 py-6 gap-6">
      <div className="w-[30%] min-w-[240px]">
        <h2 className="font-display text-sm text-text-secondary uppercase tracking-wider mb-3">Prompts</h2>
        <div className="space-y-3">
          {Object.entries(grouped).map(([key, items]) => (
            <div key={key}>
              <p className="text-xs font-body text-text-muted mb-1.5">{key}</p>
              {items.map((def) => (
                <button
                  key={`${def.key}-${def.type}`}
                  onClick={() => selectDef(def)}
                  className={`w-full flex items-center gap-2 px-2 py-1.5 rounded text-left text-sm font-body ${
                    selected?.key === def.key && selected?.type === def.type
                      ? 'bg-amber/10 text-amber border border-amber/30'
                      : 'text-text-secondary hover:bg-panel border border-transparent'
                  }`}
                >
                  <ChevronRight className="w-3 h-3" />
                  <FileText className="w-3.5 h-3.5" />
                  <span>{def.type}</span>
                  <span className="ml-auto text-xs text-text-muted">v{def.activeVersion}</span>
                </button>
              ))}
            </div>
          ))}
        </div>
      </div>
      <div className="flex-1 flex flex-col">
        {selected && (
          <>
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-display text-lg text-amber">
                {selected.key} · {selected.type}
              </h2>
            </div>
            <div className="mb-4">
              <p className="text-xs text-text-secondary mb-2 font-body">Versions:</p>
              <div className="flex gap-2 flex-wrap">
                {versions.map((v) => (
                  <div
                    key={v.version}
                    className={`flex items-center gap-1.5 px-2.5 py-1 rounded border text-xs font-body ${
                      v.version === activeVersion
                        ? 'border-amber/50 bg-amber/10 text-amber'
                        : 'border-border-base text-text-secondary'
                    }`}
                  >
                    <span>v{v.version}</span>
                    {v.version === activeVersion && <Check className="w-3 h-3" />}
                    {v.version !== activeVersion && (
                      <button
                        onClick={() => handleActivate(v.version)}
                        className="text-text-muted hover:text-amber"
                      >
                        Activate
                      </button>
                    )}
                  </div>
                ))}
              </div>
            </div>
            <div className="flex-1 flex flex-col">
              <p className="text-xs text-text-secondary mb-2 font-body">Editor:</p>
              <textarea
                value={editorContent}
                onChange={(e) => setEditorContent(e.target.value)}
                className="flex-1 min-h-[200px] bg-elevated border border-border-base rounded p-3 text-sm font-body text-text-primary focus:border-amber focus:outline-none resize-none"
              />
              <div className="flex gap-2 mt-3">
                <button
                  onClick={handleSave}
                  className="bg-amber text-base text-sm font-semibold px-4 py-2 rounded hover:shadow-[0_0_15px_rgba(255,170,0,0.4)]"
                >
                  Save as new version
                </button>
                <button
                  onClick={handleRender}
                  className="border border-border-base text-text-secondary text-sm px-4 py-2 rounded hover:border-amber hover:text-amber"
                >
                  Render Preview
                </button>
              </div>
              {rendered && (
                <div className="mt-3 bg-panel border border-border-base rounded p-3">
                  <p className="text-xs text-text-secondary mb-1 font-body">Preview:</p>
                  <pre className="text-xs font-body text-text-primary whitespace-pre-wrap">{rendered}</pre>
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
