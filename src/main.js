// main.js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'       // 引入路由配置
import messagePlugin from './plugins/message'  // 引入消息插件
import axios from 'axios'

// 设置 axios 默认配置以支持 session
axios.defaults.withCredentials = true
axios.defaults.baseURL = 'http://localhost:8080' // 后端地址

// 创建 Vue 应用实例
const app = createApp(App)

// 全局挂载路由、消息插件、axios
app
  .use(router)
  .use(messagePlugin)

// 挂载 axios 到全局属性，这样在组件里就能通过 this.$axios 调用
app.config.globalProperties.$axios = axios

// 最后挂载到 #app
app.mount('#app')
