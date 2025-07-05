<template>
    <div class="register-container">
      <div class="container">
        <h1>申请人注册</h1>
        <p class="welcome-message">填写信息注册新账号</p>
  
        <form @submit.prevent="submitRegister">
          <label for="username">用户名：</label>
          <input
            type="text"
            id="username"
            v-model.trim="form.username"
            placeholder="请输入用户名"
            required
          />
  
          <label for="password">密码：</label>
          <input
            type="password"
            id="password"
            v-model="form.password"
            placeholder="请输入密码"
            required
          />
  
          <label for="confirmPassword">确认密码：</label>
          <input
            type="password"
            id="confirmPassword"
            v-model="form.confirmPassword"
            placeholder="请再次输入密码"
            required
          />
  
          <label for="email">邮箱：</label>
          <input
            type="email"
            id="email"
            v-model.trim="form.email"
            placeholder="请输入邮箱"
            required
          />
  
          <label for="phone">联系电话：</label>
          <input
            type="tel"
            id="phone"
            v-model.trim="form.phone"
            placeholder="请输入手机号（选填）"
          />
  
          <label for="fullName">真实姓名：</label>
          <input
            type="text"
            id="fullName"
            v-model.trim="form.fullName"
            placeholder="请输入真实姓名（选填）"
          />
  
          <div class="button-container">
            <button type="submit">注册</button>
          </div>
        </form>
  
        <div class="button-container">
          <router-link to="/applicant-login" class="login-link"
            >已有账号？点击登录</router-link
          >
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "ApplicantRegister",
    data() {
      return {
        form: {
          username: "",
          password: "",
          confirmPassword: "",
          email: "",
          phone: "",
          fullName: "",
        },
      };
    },
    methods: {
      async submitRegister() {
        const {
          username,
          password,
          confirmPassword,
          email,
          phone,
          fullName,
        } = this.form;
  
        if (!username || !password || !confirmPassword || !email) {
          alert("用户名、密码、确认密码和邮箱为必填项");
          return;
        }
  
        if (password !== confirmPassword) {
          alert("两次输入的密码不一致");
          return;
        }
  
        try {
          const response = await fetch(
            "http://localhost:8080/api/applicants/register",
            {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({
                username,
                passwordHash: password, // 后端接口字段名叫passwordHash，但这里传明文，后端会加密
                email,
                phone,
                fullName,
              }),
            }
          );
  
          const result = await response.json();
  
          if (response.ok && result.status === "success") {
            alert("注册成功，跳转登录页");
            this.$router.push("/applicant-login");
          } else {
            alert("注册失败：" + (result.message || "未知错误"));
          }
        } catch (err) {
          alert("注册异常：" + err.message);
        }
      },
    },
  };
  </script>
  
  <style scoped>
  .register-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background-image: url("./img/background.jpg");
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
  
  .login-link {
    display: inline-block;
    margin-top: 20px;
    font-size: 15px;
    color: #2980b9;
    cursor: pointer;
    text-decoration: none;
  }
  
  .login-link:hover {
    text-decoration: underline;
  }
  
  @media (max-width: 400px) {
    .container {
      width: 90vw;
      padding: 30px 20px;
    }
  }
  </style>
  