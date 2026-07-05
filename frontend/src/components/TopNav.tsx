import { NavLink, useNavigate } from 'react-router-dom';
import { LogOut, Cpu } from 'lucide-react';

const NAV_ITEMS = [
  { to: '/chat', label: 'Chat' },
  { to: '/rag', label: 'RAG' },
  { to: '/prompts', label: 'Prompt' },
  { to: '/tools', label: 'Tools' },
];

export default function TopNav({
  username,
  onLogout,
}: {
  username: string | null;
  onLogout: () => void;
}) {
  const navigate = useNavigate();

  return (
    <header className="h-14 border-b border-border-base bg-panel/80 backdrop-blur-sm relative">
      <div className="absolute bottom-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-cyan/60 to-transparent" />
      <div className="h-full mx-auto max-w-7xl px-6 flex items-center justify-between">
        <div className="flex items-center gap-8">
          <div className="flex items-center gap-2">
            <Cpu className="w-5 h-5 text-cyan" />
            <span className="font-display font-bold text-text-primary tracking-wide">AI Enterprise</span>
          </div>
          <nav className="flex items-center gap-1">
            {NAV_ITEMS.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `px-3 py-1.5 text-sm font-medium transition-all relative ${
                    isActive ? 'text-cyan' : 'text-text-secondary hover:text-text-primary'
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    {item.label}
                    {isActive && (
                      <span className="absolute bottom-0 left-2 right-2 h-px bg-cyan shadow-[0_0_8px_rgba(0,212,255,0.6)]" />
                    )}
                  </>
                )}
              </NavLink>
            ))}
          </nav>
        </div>
        <div className="flex items-center gap-3">
          {username && <span className="text-xs text-text-secondary font-body">{username}</span>}
          <button
            onClick={() => {
              onLogout();
              navigate('/login');
            }}
            className="text-text-secondary hover:text-magenta transition-colors"
            title="登出"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
}
