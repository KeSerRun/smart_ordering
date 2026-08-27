import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 精简版管理端前端：Vue3 + JS，开发时把 /api 代理到后端 8080
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_SERVICE_TARGET || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})