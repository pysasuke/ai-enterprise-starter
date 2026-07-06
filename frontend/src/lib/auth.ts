const TOKEN_KEY = 'jwt_token';
const USERNAME_KEY = 'auth_username';

export const auth = {
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  },
  setToken(token: string, username: string) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USERNAME_KEY, username);
  },
  getUsername(): string | null {
    return localStorage.getItem(USERNAME_KEY);
  },
  clear() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
  },
  isAuthenticated(): boolean {
    return !!this.getToken();
  },
};
