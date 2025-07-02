<template>
  <div class="resume-page">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <h2>💁‍♀️ 前台管理系统</h2>
      <ul class="menu-list">
        <li @click="$router.push('/counter-dashboard')"><strong>个人档案</strong></li>
        <li @click="$router.push('/counter-dinein-order')">管理堂食订单</li>
        <li @click="$router.push('/manage-tables')">管理餐桌</li>

        <li>
          <strong style="margin-top: 20px; cursor: pointer; color: #fff; font-weight: bold;">
            外卖管理
          </strong>
        </li>
        <li @click="$router.push('/delivery-assign')">分配外卖员</li>
        <li class="active">添加外卖员</li>
        <li @click="$router.push('/delivery-view')" style="padding-left: 15px;">查看外卖订单</li>

        <li @click="$router.push('/counter-attendance')">考勤打卡</li>
        <li @click="$router.push('/counter-leave')">请假申请</li>
        <li @click="$router.push('/counter-leave-progress')">我的请假记录</li>
      </ul>

      <div class="logout" @click="logout">退出系统</div>
    </div>

    <!-- 内容区域 -->
    <div class="form-section">
      <h3 class="section-title">添加外卖员</h3>

      <form class="form-card" @submit.prevent="submitForm" autocomplete="off">
        <div class="form-group">
          <label for="name">姓名：</label>
          <input
            id="name"
            v-model="form.name"
            placeholder="请输入外卖员姓名"
            required
            autocomplete="off"
          />
        </div>
        <div class="form-group">
          <label for="phone">联系方式：</label>
          <input
            id="phone"
            v-model="form.phoneNumber"
            placeholder="请输入手机号"
            required
            autocomplete="off"
            pattern="^1[3-9]\\d{9}$"
            title="请输入有效的手机号"
          />
        </div>
        <button type="submit">提交</button>
      </form>

      <div v-if="message" :class="['message', success ? 'success' : 'error']" role="alert">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { ref } from "vue";

export default {
  name: "AddDeliveryPerson",
  setup() {
    const form = ref({
      name: "",
      phoneNumber: "",
    });

    const message = ref("");
    const success = ref(false);

    const submitForm = async () => {
      try {
        const response = await axios.post("http://localhost:8080/api/delivery/add", form.value);
        success.value = response.data.success;
        message.value = response.data.message;
        if (success.value) {
          form.value.name = "";
          form.value.phoneNumber = "";
        }
      } catch (error) {
        success.value = false;
        message.value = "请求出错，请稍后重试";
      }
    };

    const logout = () => {
      localStorage.removeItem("counterId");
      window.location.href = "/login";
    };

    return { form, message, success, submitForm, logout };
  },
};
</script>

<style scoped>
.resume-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}
.sidebar {
  width: 240px;
  background: #1d3557;
  color: white;
  padding: 30px 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.sidebar h2 {
  margin-bottom: 30px;
  font-size: 22px;
  border-bottom: 2px solid #fff;
  padding-bottom: 10px;
}
.menu-list {
  flex: 1;
  list-style: none;
  padding-left: 0;
  margin: 0;
  overflow-y: auto;
}
.menu-list li {
  padding: 10px 0;
  font-size: 15px;
  cursor: pointer;
  color: #ccc;
  user-select: none;
}
.menu-list li.active {
  color: #00b4d8;
  font-weight: bold;
}
.logout {
  color: #ffb3b3;
  transition: color 0.3s ease;
}
.logout:hover {
  color: #ffffff;
  font-weight: bold;
}

.form-section {
  flex: 1;
  background: #f9fbfd;       
  padding: 40px 60px;
  box-sizing: border-box;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  align-items: center;      
}

.form-section h3 {
  font-size: 26px;          
  margin-bottom: 30px;
  border-left: 6px solid #007bff;
  padding-left: 14px;
  color: #0056b3;          
  font-weight: 700;
  align-self: flex-start;    
}

.form-card {
  max-width: 600px;
  width: 100%;               
  margin: 0 auto;
  padding: 32px 36px;        
  background: #fff;
  border-radius: 14px;       
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);  
  transition: box-shadow 0.3s ease;
}

.form-card:hover {
  box-shadow: 0 12px 36px rgba(0,0,0,0.12);
}

.form-group {
  margin-bottom: 2rem;      
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 600;
  font-size: 1.1rem;
  margin-bottom: 0.6rem;
  color: #333;
  user-select: none;
}

.form-group input {
  width: 100%;
  padding: 14px 18px;        
  border-radius: 8px;
  border: 1.8px solid #ccc;
  font-size: 1rem;
  transition: border-color 0.25s ease;
  outline-offset: 2px;
}

.form-group input:focus {
  border-color: #007bff;
  box-shadow: 0 0 6px #007bffaa;
}

button {
  width: 100%;
  padding: 14px 0;
  background-color: #28a745;
  color: white;
  font-weight: 600;
  font-size: 1.1rem;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #218838;
}

.message {
  margin-top: 1.5rem;
  padding: 14px 20px;
  border-radius: 8px;
  text-align: center;
  font-size: 1rem;
  max-width: 600px;
  user-select: none;
  box-sizing: border-box;
}

.success {
  background-color: #d4edda;
  color: #155724;
  border: 1.5px solid #c3e6cb;
}

.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1.5px solid #f5c6cb;
}

</style>
