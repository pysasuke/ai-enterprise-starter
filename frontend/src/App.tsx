import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import TopNav from './components/TopNav';
import RequireAuth from './components/RequireAuth';
import Login from './routes/login';
import Chat from './routes/chat';
import Rag from './routes/rag';
import Prompts from './routes/prompts';
import Tools from './routes/tools';

function Shell() {
  const { username, logout } = useAuth();

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/*"
          element={
            <RequireAuth>
              <div className="min-h-screen flex flex-col">
                <TopNav username={username} onLogout={logout} />
                <Routes>
                  <Route path="/" element={<Navigate to="/chat" replace />} />
                  <Route path="/chat" element={<Chat />} />
                  <Route path="/rag" element={<Rag />} />
                  <Route path="/prompts" element={<Prompts />} />
                  <Route path="/tools" element={<Tools />} />
                </Routes>
              </div>
            </RequireAuth>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <Shell />
      </AuthProvider>
    </ToastProvider>
  );
}
