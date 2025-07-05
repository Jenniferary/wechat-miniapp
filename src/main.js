import { createApp } from 'vue';
import App from './App.vue';
import router from './router';  // 引入路由配置
import messagePlugin from './plugins/message';  // 引入消息插件

// 创建 Vue 应用并挂载路由
createApp(App)
  .use(router)  // 使用路由
  .use(messagePlugin)  // 使用消息插件
  .mount('#app');
