<template>
  <div class="login-container">
    <div class="form-container">
      <h1>店长登录</h1>

      <form @submit.prevent="submitLogin">
        <label for="username">用户名：</label>
        <input
          type="text"
          id="username"
          v-model="username"
          placeholder="请输入用户名"
          required
        />

        <label for="password">密码：</label>
        <input
          type="password"
          id="password"
          v-model="password"
          placeholder="请输入密码"
          required
        />

        <button type="submit">登录</button>
      </form>

      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      username: "",
      password: "",
      errorMessage: ""
    };
  },
  methods: {
    async submitLogin() {
      const { username, password } = this;
      
      try {
        const response = await fetch("http://localhost:8080/api/branch-managers/login", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ username, password })
        });
        
        const result = await response.json();

        if (response.ok && result.status === "success") {
          // 登录成功，保存店长信息并跳转
          localStorage.setItem("managerId", result.data.id);  // 保存 managerId
          localStorage.setItem("branchId", result.data.branchId);  // 保存 branchId
          localStorage.setItem("managerName", result.data.name);  // 保存 managerName
          this.$router.push("/branch-dashboard"); // 跳转到店长管理界面
        } else {
          this.errorMessage = result.message || "登录失败，请重试";
        }
      } catch (error) {
        this.errorMessage = "网络错误，请稍后再试";
      }
    }
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f4f4f4;
}

.form-container {
  background-color: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

h1 {
  text-align: center;
  margin-bottom: 20px;
}

label {
  display: block;
  font-size: 16px;
  margin-top: 10px;
}

input {
  width: 100%;
  padding: 10px;
  font-size: 16px;
  margin-top: 5px;
  margin-bottom: 20px;
  border-radius: 4px;
  border: 1px solid #ccc;
}

button {
  width: 100%;
  padding: 10px;
  font-size: 18px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #2980b9;
}

.error-message {
  color: red;
  text-align: center;
  margin-top: 20px;
}
</style>
