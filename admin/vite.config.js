import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 精简版管理端前端：Vue3 + JS，开发时把 /api 代理到后端 8080
export default defineConfig({
  plugins: [vue()],
  // sockjs-client 等 CommonJS 库在模块顶层引用 Node 的 global，
  // 浏览器环境没有 global，用 globalThis 顶替（sockjs 官方同款修复）。
  // 注意：config.define 只作用于源码与构建，预打包依赖必须再走 optimizeDeps.esbuildOptions.define
  define: {
    global: 'globalThis'
  },
  optimizeDeps: {
    esbuildOptions: {
      define: {
        global: 'globalThis'
      }
    }
  },
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
        changeOrigin: true,
        ws: true // WebSocket( SockJS) 升级也走代理，后厨大屏 STOMP 需要
      }
    }
  }
})