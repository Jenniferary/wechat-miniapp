<template>
  <div class="all-login-container">
    <!-- 标题部分 -->
    <div class="header">
      <h1>食尚阁后台管理系统</h1>
    </div>

    <!-- 选项框部分 -->
    <div class="options-container">
      <div class="option" @click="navigateTo('branch-login')">
        <div class="option-icon">🏠</div>
        <h2>我是店长</h2>
        <p>点击进入店长登录页面</p>
      </div>

      <div class="option" @click="navigateTo('login')">
        <div class="option-icon">👨‍💼</div>
        <h2>我是员工</h2>
        <p>点击进入员工登录页面</p>
      </div>

      <div class="option" @click="navigateTo('franchise')">
        <div class="option-icon">💼</div>
        <h2>申请加盟</h2>
        <p>点击进入加盟申请页面</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    navigateTo(page) {
      if (page === 'branch-login') {
        this.$router.push('/branch-login');
      } else if (page === 'login') {
        this.$router.push('/login');
      } else if (page === 'franchise') {
        this.$router.push('/franchise');
      }
    }
  }
};
</script>

<style scoped>
.all-login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: transparent; /* 设置背景为透明 */
  font-family: 'Arial', sans-serif;
}


.header {
  text-align: center;
  margin-bottom: 40px;
}

h1 {
  font-size: 48px;
  color: #3498db;
  text-shadow: 2px 2px 5px rgba(0, 0, 0, 0.3);
}

.options-container {
  display: flex;
  justify-content: space-around;
  width: 80%;
  max-width: 900px;
  margin: 0 auto;
}

.option {
  background-color: rgba(255, 255, 255, 0.8); /* 透明背景 */
  border: 1px solid #ddd;
  border-radius: 15px;
  padding: 20px;
  width: 40%;
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.option:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.2);
}

.option-icon {
  font-size: 40px;
  margin-bottom: 15px;
}

h2 {
  font-size: 24px;
  color: #3498db;
  margin-bottom: 10px;
}

p {
  font-size: 16px;
  color: #555;
}

@media (max-width: 768px) {
  .options-container {
    flex-direction: column;
    align-items: center;
  }

  .option {
    width: 80%;
    margin-bottom: 20px;
  }
}
</style>
