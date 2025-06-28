import { createApp } from 'vue';
import App from './App.vue';
import router from './router';  // 引入路由配置

// 创建 Vue 应用并挂载路由
createApp(App)
  .use(router)  // 使用路由
  .mount('#app');
