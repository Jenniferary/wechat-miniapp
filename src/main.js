import { createApp } from 'vue';
import App from './App.vue';
import router from './router';  // 引入路由配置
import axios from 'axios';

// 创建 Vue 应用并挂载路由
createApp(App)
  .use(router)  // 使用路由
  .mount('#app');




// 设置axios默认配置以支持session
axios.defaults.withCredentials = true
axios.defaults.baseURL = 'http://localhost:8080' // 您的后端地址

// 将axios添加到全局属性
const app = createApp(App)
app.config.globalProperties.$axios = axios
app.use(router)
app.mount('#app')