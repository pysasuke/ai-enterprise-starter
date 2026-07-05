import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        base: '#0a0e14',
        panel: '#11161f',
        elevated: '#1a2030',
        'border-base': '#1f2a3d',
        'border-glow': '#2a3a55',
        'text-primary': '#e6edf3',
        'text-secondary': '#8b96a8',
        'text-muted': '#4a5568',
        cyan: '#00d4ff',
        magenta: '#ff2d92',
        green: '#00ff88',
        amber: '#ffaa00',
      },
      fontFamily: {
        display: ['"Space Grotesk"', 'sans-serif'],
        body: ['"JetBrains Mono"', 'ui-monospace', 'monospace'],
      },
    },
  },
  plugins: [],
} satisfies Config;
