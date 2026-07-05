import { createContext, useContext, ReactNode, useState } from 'react';
import { api } from '../lib/api';
import { auth } from '../lib/auth';

interface AuthCtx {
  isAuthenticated: boolean;
  username: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const Ctx = createContext<AuthCtx | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [username, setUsername] = useState<string | null>(auth.getUsername());

  const login = async (username: string, password: string) => {
    const res = await api.auth.login(username, password);
    auth.setToken(res.data.token, res.data.username);
    setUsername(res.data.username);
  };

  const logout = () => {
    auth.clear();
    setUsername(null);
  };

  return (
    <Ctx.Provider value={{ isAuthenticated: !!username, username, login, logout }}>
      {children}
    </Ctx.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
