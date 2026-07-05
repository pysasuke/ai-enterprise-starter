import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Cpu } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export default function Login() {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const { show } = useToast();
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await login(username, password);
      show('success', '登录成功');
      navigate('/chat');
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : '登录失败';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid-texture flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="flex flex-col items-center mb-8">
          <Cpu className="w-10 h-10 text-cyan mb-3" />
          <h1 className="font-display text-3xl font-bold text-text-primary">AI Enterprise</h1>
          <p className="text-sm text-text-secondary mt-1">Admin Console</p>
        </div>
        <form
          onSubmit={handleSubmit}
          className="bg-panel border border-border-base rounded-lg p-6 space-y-4 glow-hover"
        >
          <div>
            <label className="block text-xs text-text-secondary mb-1.5 font-body">Username</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className={`w-full bg-elevated border ${
                error ? 'border-magenta' : 'border-border-base'
              } rounded px-3 py-2 text-text-primary font-body text-sm focus:border-cyan focus:outline-none focus:shadow-[0_0_0_2px_rgba(0,212,255,0.15)]`}
              placeholder="admin"
            />
          </div>
          <div>
            <label className="block text-xs text-text-secondary mb-1.5 font-body">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={`w-full bg-elevated border ${
                error ? 'border-magenta' : 'border-border-base'
              } rounded px-3 py-2 text-text-primary font-body text-sm focus:border-cyan focus:outline-none focus:shadow-[0_0_0_2px_rgba(0,212,255,0.15)]`}
              placeholder="••••••••"
            />
          </div>
          {error && <p className="text-magenta text-xs font-body">{error}</p>}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-cyan text-base font-semibold py-2 rounded hover:shadow-[0_0_20px_rgba(0,212,255,0.4)] transition-shadow disabled:opacity-50"
          >
            {loading ? '→ Loading...' : '→ Enter Console'}
          </button>
          <p className="text-center text-xs text-text-muted font-body">默认: admin / admin123</p>
        </form>
      </div>
    </div>
  );
}
