<template>
  <div class="login-container">
    <div class="container">
      <h1>欢迎光临食尚阁</h1>
      <p class="welcome-message">美味等待着您</p>

      <form @submit.prevent="submitLogin">
        <label for="role">请选择身份：</label>
        <select id="role" v-model="role" required>
  
          <option value="counter">前台</option>
          <option value="hr">HR</option>
          <option value="chef">厨师</option>
          <option value="waiter">服务员</option>
        </select>

        <label for="username">用户名：</label>
        <input type="text" id="username" v-model="username" required />

        <label for="password">密码：</label>
        <input type="password" id="password" v-model="password" required />

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
  data() {
    return {
      username: "",
      password: "",
      role: "admin", // 默认管理员
    };
  },
  methods: {
    async submitLogin() {
      const { username, password, role } = this;

      if (role === "admin") {
        try {
          const response = await fetch("http://localhost:8080/api/manager/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
          });
          const result = await response.json();
          if (response.ok && result.status === "success") {
            alert("管理员登录成功");
            localStorage.setItem("role", "admin");
            this.$router.push("/admin");
          } else {
            alert("管理员账号或密码错误");
          }
        } catch (error) {
          alert("管理员登录失败：" + error.message);
        }
      }

      else if (role === "counter") {
        try {
          const response = await fetch("http://localhost:8080/api/counter/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
          });
          const result = await response.json();
          if (response.ok && result.status === "success") {
            alert("前台登录成功");
            localStorage.setItem("role", "counter");
            localStorage.setItem("username", username);
            localStorage.setItem("counterId", result.data.id); 
            localStorage.setItem('branchId', result.data.branchId);
            this.$router.push("/counter-dashboard");
          } else {
            alert(result.message || "前台账号或密码错误");
          }
        } catch (error) {
          alert("前台登录失败：" + error.message);
        }
      }

      else if (role === "hr") {
        try {
          const response = await fetch("http://localhost:8080/api/hr/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
          });
          const result = await response.json();
          if (response.ok && result.status === "success") {
            alert("HR登录成功");
            localStorage.setItem("role", "hr");
            localStorage.setItem("username", username);
            localStorage.setItem("hrId", result.data.id);
            this.$router.push("/hr-dashboard");
          } else {
            alert("HR账号或密码错误");
          }
        } catch (error) {
          alert("HR登录失败：" + error.message);
        }
      }

      else if (role === "chef") {
        try {
          const response = await fetch("http://localhost:8080/api/chef/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
          });
          const result = await response.json();
          if (response.ok && result.status === "success") {
            alert("厨师登录成功");
            localStorage.setItem("role", "chef");
            localStorage.setItem("username", username);
            localStorage.setItem("chefId", result.data.id);
            this.$router.push("/chef-dashboard");
          } else {
            alert("厨师账号或密码错误");
          }
        } catch (error) {
          alert("厨师登录失败：" + error.message);
        }
      }

      else if (role === "waiter") {
        try {
          const response = await fetch("http://localhost:8080/api/waiters/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
          });
          const result = await response.json();
          if (response.ok && result.status === "success") {
            alert("服务员登录成功");
            localStorage.setItem("role", "waiter");
            localStorage.setItem("username", username);
            localStorage.setItem("waiterId", result.data.id);
            this.$router.push("/waiter-dashboard");
          } else {
            alert("服务员账号或密码错误");
          }
        } catch (error) {
          alert("服务员登录失败：" + error.message);
        }
      }

      else {
        alert("请选择正确的身份");
      }
    },
  },
};
</script>

<style>
/* 样式保持不变 */
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
