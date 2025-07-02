<template>
  <div class="login-container">
    <div class="container">
      <h1>欢迎回来，店长！</h1>
      <p class="welcome-message">管理您的门店从这里开始</p>

      <form @submit.prevent="submitLogin">
        <label for="username">用户名：</label>
        <input type="text" id="username" v-model="username" required />

        <label for="password">密码：</label>
        <input type="password" id="password" v-model="password" required />

        <div class="button-container">
          <button type="submit">登录</button>
        </div>
      </form>

      <div class="button-container">
        <a href="/register" class="login-link">还没有账号？点击注册</a>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      username: '',
      password: ''
    };
  },
  methods: {
    async submitLogin() {
      try {
        const response = await fetch('http://localhost:8080/api/branch-managers/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: this.username,
            password: this.password
          })
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
          alert('店长登录成功');
          localStorage.setItem('managerId', result.data.id);
          localStorage.setItem('branchId', result.data.branchId);
          localStorage.setItem('managerName', result.data.name);
          this.$router.push('/branch-dashboard');
        } else {
          alert(result.message || '账号或密码错误');
        }
      } catch (err) {
        alert('登录失败：' + err.message);
      }
    }
  }
};
</script>

<style scoped>
body {
  margin: 0;
  font-family: Arial, sans-serif;
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-size: cover;
  background-position: center;
}

.container {
  text-align: center;
  padding: 30px 40px;
  background-color: rgba(255, 255, 255, 0.88);
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
}

h1 {
  font-size: 32px;
  margin-bottom: 10px;
}

.welcome-message {
  font-size: 18px;
  margin-bottom: 25px;
  color: #555;
}

label {
  display: block;
  font-size: 16px;
  margin-top: 20px;
  text-align: left;
}

input {
  width: 280px;
  padding: 10px;
  font-size: 15px;
  border: 1px solid #ccc;
  border-radius: 6px;
  margin-top: 8px;
  box-sizing: border-box;
}

button {
  width: 100%;
  padding: 10px;
  font-size: 18px;
  background-color: #3498db;
  color: #fff;
  border: none;
  border-radius: 6px;
  margin-top: 25px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #2980b9;
}

.login-link {
  display: block;
  margin-top: 20px;
  font-size: 15px;
  color: #333;
  text-decoration: none;
}

.login-link:hover {
  text-decoration: underline;
}
</style>
