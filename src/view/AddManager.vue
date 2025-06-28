<template>
  <div class="container">
    <!-- Sidebar -->
    <div class="sidebar">
      <h3>前台管理系统</h3>
      <ul>
        <li><router-link to="/counter">🏠 回到管理主页</router-link></li>
        <li><router-link to="/manage-delivery">🛵 外卖员管理</router-link></li>
        <li><router-link to="/check-orders">💵 查看订单及结账</router-link></li>
        <li><router-link to="/distribute-coupons">🎟️ 分配优惠券</router-link></li>
        <li><router-link to="/manage-tables">🪑 餐桌预定</router-link></li>
      </ul>
    </div>

    <!-- Content -->
    <div class="content">
      <h2 class="page-title">👤➕ 添加管理员</h2>
      <form class="form-card" @submit.prevent="submitForm">
        <div class="form-group">
          <label>账号：</label>
          <input v-model="form.username" placeholder="请输入管理员账号" required />
        </div>
        <div class="form-group">
          <label>密码：</label>
          <input type="password" v-model="form.password" placeholder="请输入密码" required />
        </div>
        <button type="submit">提交</button>
      </form>

      <div v-if="message" :class="{ success: success, error: !success }" class="message">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { ref } from "vue";

export default {
  name: "AddManager",
  setup() {
    const form = ref({
      username: "",
      password: "",
    });

    const message = ref("");
    const success = ref(false);

    const submitForm = async () => {
      try {
        const response = await axios.post("http://localhost:8080/api/addmanager", form.value);
        // 后端返回的是字符串 "true"，这里要做严格等值判断
        success.value = response.data.success === "true";
        message.value = response.data.message;

        if (success.value) {
          form.value.username = "";
          form.value.password = "";
        }
      } catch (error) {
        console.error("添加管理员失败：", error); // 打印错误信息
        success.value = false;
        message.value = "请求出错，请稍后重试";
      }
    };

    return { form, message, success, submitForm };
  },
};
</script>

<style scoped>
.container {
  display: flex;
  max-width: 1200px;
  margin: 40px auto;
  padding: 40px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}
.sidebar {
  background-color: rgba(255, 255, 255, 0.8);
  padding: 20px;
  width: 200px;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}
.sidebar h3 {
  margin-bottom: 10px;
  font-size: 20px;
  color: #2980b9;
  border-bottom: 2px solid #2980b9;
  padding-bottom: 10px;
}
.sidebar ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}
.sidebar ul li {
  margin-bottom: 10px;
}
.sidebar ul li a {
  text-decoration: none;
  color: #333;
  font-size: 16px;
  transition: color 0.3s ease;
}
.sidebar ul li a:hover {
  color: #2980b9;
  font-weight: bold;
}
.content {
  flex-grow: 1;
  padding: 2rem;
  width: 760px;
  background-color: #fafafa;
  margin-top: 50px;
}
.page-title {
  text-align: center;
  font-size: 1.8rem;
  color: #2980b9;
}
.form-card {
  max-width: 600px;
  margin: 0 auto;
  padding: 2rem;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 0 10px #ccc;
}
.form-group {
  margin-bottom: 1rem;
}
.form-group label {
  font-weight: bold;
  display: block;
  margin-bottom: 0.3rem;
}
.form-group input {
  width: 100%;
  padding: 0.5rem;
  border-radius: 6px;
  border: 1px solid #ccc;
}
button {
  width: 100%;
  padding: 0.8rem;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
}
button:hover {
  background-color: #218838;
}
.message {
  margin-top: 1rem;
  padding: 0.7rem;
  border-radius: 6px;
  text-align: center;
}
.success {
  background-color: #d4edda;
  color: #155724;
}
.error {
  background-color: #f8d7da;
  color: #721c24;
}
</style>
