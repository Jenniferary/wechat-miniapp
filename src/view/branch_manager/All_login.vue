<template>
  <div class="all-login-container">
    <!-- 标题部分 -->
    <div class="header">
      <h1>食尚阁后台管理系统</h1>
    </div>

    <!-- 选项框部分 -->
    <div class="options-container">
      <div class="option" @click="navigateTo('branch-login')">
        <h2>我是店长</h2>
        <p>点击进入店长登录页面</p>
      </div>

      <div class="option" @click="navigateTo('login')">
        <h2>我是员工</h2>
        <p>点击进入员工登录页面</p>
      </div>

      <div class="option" @click="navigateTo('franchise')">
        <h2>申请加盟</h2>
        <p>点击进入加盟申请页面</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    // 跳转到不同页面
    navigateTo(page) {
      if (page === 'branch-login') {
        this.$router.push('/branch-login'); // 跳转到店长登录页面
      } else if (page === 'login') {
        this.$router.push('/login'); // 跳转到员工登录页面
      } else if (page === 'franchise') {
        this.$router.push('/franchise'); // 跳转到加盟申请页面
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
  background-color: #f4f4f4;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

h1 {
  font-size: 40px;
  color: #3498db;
}

.options-container {
  display: flex;
  justify-content: space-around;
  width: 100%;
}

.option {
  background-color: #ffffff;
  border: 1px solid #ccc;
  border-radius: 10px;
  padding: 20px;
  width: 30%;
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.option:hover {
  transform: scale(1.05);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

h2 {
  font-size: 24px;
  color: #3498db;
}

p {
  font-size: 16px;
  color: #777;
}
</style>
