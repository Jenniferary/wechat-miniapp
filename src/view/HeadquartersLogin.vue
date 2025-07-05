<template>
  <div class="headquarters-login-container">
    <div class="login-card">
      <div class="left-section">
        <h1>🏢 总部登录</h1>
        <p>欢迎使用食尚阁总部管理系统</p>
      </div>
      <div class="right-section">
        <form @submit.prevent="login" class="login-form">
          <div class="form-group">
            <label for="username">用户名</label>
            <input
              type="text"
              id="username"
              v-model="loginForm.username"
              placeholder="请输入总部用户名"
              required
            />
          </div>

          <div class="form-group">
            <label for="password">密码</label>
            <input
              type="password"
              id="password"
              v-model="loginForm.password"
              placeholder="请输入密码"
              required
            />
          </div>

          <div class="form-group">
            <label for="department">部门</label>
            <select id="department" v-model="loginForm.department" required>
              <option value="">请选择部门</option>
              <option value="headquarters">总部</option>
            </select>
          </div>

          <button type="submit" class="login-btn" :disabled="loading">
            <span v-if="loading">登录中...</span>
            <span v-else>登录</span>
          </button>
        </form>

        <div class="footer">
          <p>如有问题，请联系系统管理员</p>
          <a href="#" @click="$router.push('/all-login')" class="back-link">
            ← 返回选择页面
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HeadquartersLogin',
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
        department: ''
      },
      loading: false
    }
  },
  methods: {
    async login() {
      this.loading = true;
      try {
        const response = await fetch('http://localhost:8080/api/headquarters/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include',
          body: JSON.stringify(this.loginForm)
        });
        const result = await response.json();
        if (result.status === 'success') {
          localStorage.setItem('userRole', 'headquarters');
          localStorage.setItem('username', this.loginForm.username);
          localStorage.setItem('department', this.loginForm.department);
          localStorage.setItem('userId', result.data.id);
          this.$router.push('/franchise-management');
          alert('登录成功！');
        } else {
          alert('登录失败：' + result.message);
        }
      } catch (error) {
        console.error('登录失败:', error);
        alert('登录失败，请检查网络连接！');
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
/* .headquarters-login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f7fa;
  font-family: 'Arial', sans-serif;
} */

.login-card {
  display: flex;
  flex-direction: row;
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 900px;
  width: 100%;
  overflow: hidden;
  margin: 20px;
}

.left-section {
  flex: 1;
  background: #f5f7fa;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: center;
}

.left-section h1 {
  font-size: 30px;
  color: #333;
  margin-bottom: 15px;
}

.left-section p {
  font-size: 16px;
  color: #666;
}

.right-section {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-form {
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 10px;
  font-size: 16px;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #667eea;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.footer {
  text-align: center;
}

.footer p {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
}

.back-link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.back-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
  }

  .left-section,
  .right-section {
    padding: 30px 20px;
  }

  .left-section h1 {
    font-size: 24px;
  }
}
</style>
