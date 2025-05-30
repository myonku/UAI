import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      'vue': 'vue/dist/vue.esm-bundler.js',
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 5173,     // 客户端的运行端口，绑定vue运行的端口
    host: 'localhost', // 客户端的运行地址，此处也可以绑定vue运行的域名，当然也可以写在pycharm下
    // 跨域代理
    proxy: {
      '/api': {
        //  /api 路径的请求都映射到 target 属性  /api/header  ---> http://xxx:8000/header
        target: 'http://127.0.0.1:8080/',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, '')
      }
    }
  }
})
