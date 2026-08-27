import { createApp } from 'vue'
import { createPinia } from 'pinia'
import naive from 'naive-ui'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(naive) // 注册 naive-ui 全局组件，否则 <n-*> 无法解析
app.use(router)
app.mount('#app')