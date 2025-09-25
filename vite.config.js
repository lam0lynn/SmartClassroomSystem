import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
export default defineConfig({
  plugins: [
    vue(),

  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, '')
      },
      '/img': {
        target: 'http://localhost:8088', // 你的后端端口
        changeOrigin: true,
      },
      '/images': {
        target: 'http://localhost:8088', // 让图片访问也能代理
        changeOrigin: true,
      }
    },
  },
})
