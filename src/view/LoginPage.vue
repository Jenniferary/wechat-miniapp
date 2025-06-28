<template>
  <div class="login-container">
    <div class="container">
      <h1>欢迎光临食尚阁</h1>
      <p class="welcome-message">美味等待着您</p>

      <form @submit.prevent="submitLogin">
        <label for="username">用户名：</label>
        <input
          id="username"
          type="text"
          v-model="username"
          placeholder="请输入前台用户名"
          required
        />

        <label for="password">密码：</label>
        <input
          id="password"
          type="password"
          v-model="password"
          placeholder="请输入密码"
          required
        />

        <div class="button-container">
          <button type="submit">登录</button>
        </div>
      </form>

      <div class="button-container">
        <a href="/register" class="login-link">没有密码，点击注册</a>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginPage',
  data() {
    return {
      username: '',
      password: ''
    };
  },
  methods: {
    async submitLogin() {
      try {
        const response = await fetch('http://localhost:8080/api/manager/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            username: this.username,
            password: this.password
          })
        });
        const result = await response.json();
        // 根据后端返回的 status 字段判断登录结果
        if (response.ok && result.status === 'success') {
          alert('前台登录成功');
          // 可存储管理员信息
          localStorage.setItem('user', JSON.stringify({ username: this.username }));
          this.$router.push('/counter');
        } else {
          alert(result.message || '用户名或密码错误');
        }
      } catch (err) {
        console.error(err);
        alert('登录出错：' + err.message);
      }
    }
  }
};
</script>

<style scoped>

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

input,
select {
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