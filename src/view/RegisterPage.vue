<template>
  <div class="container">
    <div class="header">
      <h1>注册</h1>
    </div>
    <form @submit.prevent="handleRegister">
      <label for="username">用户名：</label>
      <input type="text" v-model="username" id="username" />

      <label for="password">密码：</label>
      <input type="password" v-model="password" id="password" />

      <br />
      <button type="submit">注册</button>
    </form>
    <p>
      <router-link to="/login" class="login-link">返回登录页面</router-link>
    </p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const username = ref('')
const password = ref('')
const router = useRouter()

const handleRegister = async () => {
  try {
    await axios.post('http://localhost:8080/api/register', {
      username: username.value,
      password: password.value
    })
    alert('注册成功！')
    router.push('/login')
  } catch (e) {
    alert('注册失败: ' + (e.response?.data?.message || '服务器错误'))
  }
}
</script>

<style>
body {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  margin: 0;
  background-size: cover;
  background-image: url("./img/background.jpg");
  color: black;
  font-family: Arial, sans-serif;
}

.container {
  text-align: center;
  padding: 20px;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

h1 {
  font-size: 36px;
  margin-bottom: 20px;
}

label {
  display: block;
  font-size: 18px;
  margin-top: 20px;
}

input[type="text"],
input[type="password"] {
  width: 300px;
  padding: 10px;
  font-size: 16px;
  border: 1px solid #ccc;
  border-radius: 5px;
  margin-top: 10px;
  box-sizing: border-box;
}

button {
  padding: 10px 20px;
  font-size: 18px;
  background-color: #3498db;
  color: #fff;
  border: none;
  border-radius: 5px;
  margin-top: 20px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #2980b9;
}

p {
  font-size: 24px;
  margin-top: 20px;
}

.login-link {
  color: black;
  font-size: 16px;
  text-decoration: none;
}

.login-link:hover {
  text-decoration: underline;
}
</style>

