<template>
    <div class="login-container">
      <div class="container">
        <h1>欢迎来到食尚阁职位申请</h1>
        <p class="welcome-message">请输入您的账号密码进行登录</p>
  
        <form @submit.prevent="submitLogin">
          <label for="username">用户名：</label>
          <input
            type="text"
            id="username"
            v-model.trim="username"
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
  
          <div class="button-container">
            <button type="submit">登录</button>
          </div>
        </form>
  
        <div class="button-container">
          <router-link to="/applicant-register" class="register-link"
            >没有账号？点击注册</router-link
          >
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "ApplicantLogin",
    data() {
      return {
        username: "",
        password: "",
      };
    },
    methods: {
      async submitLogin() {
        if (!this.username || !this.password) {
          alert("用户名和密码不能为空");
          return;
        }
  
        try {
          const response = await fetch(
            "http://localhost:8080/api/applicants/login",
            {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
              },
              body: JSON.stringify({
                username: this.username,
                password: this.password,
              }),
            }
          );
  
          const result = await response.json();
  
          if (response.ok && result.status === "success") {
            alert("登录成功");
            localStorage.setItem("applicantUsername", this.username);
            localStorage.setItem("applicantId", result.data.id);
            this.$router.push("/join-us"); // 登录后跳转页面
          } else {
            alert("登录失败：" + (result.message || "账号或密码错误"));
          }
        } catch (err) {
          alert("登录异常：" + err.message);
        }
      },
    },
  };
  </script>
  
  <style scoped>
  .login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background-size: cover;
    background-position: center;
    font-family: Arial, sans-serif;
    color: #222;
  }
  
  .container {
    background: rgba(255 255 255 / 0.9);
    padding: 40px 50px;
    border-radius: 12px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
    width: 360px;
    text-align: center;
  }
  
  h1 {
    font-size: 28px;
    margin-bottom: 12px;
    color: #2c3e50;
  }
  
  .welcome-message {
    font-size: 16px;
    margin-bottom: 30px;
    color: #34495e;
  }
  
  label {
    display: block;
    text-align: left;
    font-weight: 600;
    margin-top: 18px;
    color: #34495e;
  }
  
  input {
    width: 100%;
    padding: 10px 14px;
    font-size: 15px;
    margin-top: 6px;
    border: 1.5px solid #bbb;
    border-radius: 6px;
    box-sizing: border-box;
    transition: border-color 0.3s ease;
  }
  
  input:focus {
    outline: none;
    border-color: #3498db;
  }
  
  .button-container {
    margin-top: 30px;
  }
  
  button {
    width: 100%;
    padding: 12px 0;
    font-size: 17px;
    font-weight: 600;
    color: #fff;
    background-color: #3498db;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.3s ease;
  }
  
  button:hover {
    background-color: #2980b9;
  }
  
  .register-link {
    display: inline-block;
    margin-top: 20px;
    font-size: 15px;
    color: #2980b9;
    cursor: pointer;
    text-decoration: none;
  }
  
  .register-link:hover {
    text-decoration: underline;
  }
  
  @media (max-width: 400px) {
    .container {
      width: 90vw;
      padding: 30px 20px;
    }
  }
  </style>
  