import { auth } from './auth';

const API_BASE = '/api';

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = auth.getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...((options.headers as Record<string, string>) || {}),
  };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (res.status === 401) {
    auth.clear();
    window.location.href = '/login';
    throw new ApiError(401, 'Unauthorized');
  }
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new ApiError(res.status, err.message || `HTTP ${res.status}`);
  }
  if (res.status === 204) {
    return undefined as T;
  }
  return res.json();
}

export interface PromptDefinition {
  key: string;
  type: string;
  description: string;
  activeVersion: number;
  activeContentPreview: string;
}

export interface PromptVersion {
  key: string;
  type: string;
  version: number;
  content: string;
  active: boolean;
  createdAt: string;
}

export const api = {
  auth: {
    login: (username: string, password: string) =>
      request<{ code: number; data: { token: string; username: string } }>('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      }),
  },
  rag: {
    listDocuments: () =>
      request<
        Array<{
          id: number;
          filename: string;
          contentType: string;
          chunkCount: number;
          status: string;
        }>
      >('/rag/documents'),
    uploadDocument: (file: File) => {
      const fd = new FormData();
      fd.append('file', file);
      const headers: Record<string, string> = {};
      const token = auth.getToken();
      if (token) headers['Authorization'] = `Bearer ${token}`;
      return fetch(`${API_BASE}/rag/documents`, { method: 'POST', headers, body: fd }).then((r) => {
        if (!r.ok) throw new ApiError(r.status, 'Upload failed');
        return r.json();
      });
    },
    deleteDocument: (id: number) => request<void>(`/rag/documents/${id}`, { method: 'DELETE' }),
  },
  prompts: {
    list: () => request<PromptDefinition[]>('/prompts'),
    getVersions: (key: string, type: string) =>
      request<PromptVersion[]>(`/prompts/${key}/${type}/versions`),
    createVersion: (key: string, type: string, content: string) =>
      request(`/prompts/${key}/${type}/versions`, {
        method: 'POST',
        body: JSON.stringify({ content }),
      }),
    activate: (key: string, type: string, version: number) =>
      request(`/prompts/${key}/${type}/active`, {
        method: 'PUT',
        body: JSON.stringify({ version }),
      }),
    render: (key: string, type: string, variables: Record<string, string>) =>
      request<{ rendered: string }>('/prompts/render', {
        method: 'POST',
        body: JSON.stringify({ key, type, variables }),
      }),
  },
  tools: {
    list: () => request<Array<{ name: string; description: string; readOnly: boolean }>>('/tools'),
  },
};
