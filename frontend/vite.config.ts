import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'

// vite-plugin-vue-devtools is disabled: it crashes on startup under this Node version
// (TypeError: localStorage.getItem is not a function inside @vue/devtools-kit).
// It's a dev-convenience plugin only, not required for the app to run.
// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueJsx(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    // The app calls /api/... relatively. In production Spring Boot serves both the app and
    // the API from one origin; this proxy reproduces that during `npm run dev`, so the same
    // code path is exercised in both and CORS never enters the picture.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
