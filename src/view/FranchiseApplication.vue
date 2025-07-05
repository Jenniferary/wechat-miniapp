<template>
  <div class="franchise-application">
    <div class="header">
      <h1>加盟申请</h1>
      <p>欢迎加入我们的餐厅连锁大家庭</p>
    </div>

    <div class="form-container">
      <form @submit.prevent="submitApplication">
        <div class="form-grid-4col">
          <!-- 申请人信息 -->
          <div class="form-section">
            <h3>申请人信息</h3>
            <div class="form-subgrid">
              <div class="form-group">
                <label>姓名 *</label>
                <input v-model="form.applicantName" type="text" required placeholder="请输入您的姓名">
              </div>
              <div class="form-group">
                <label>手机号 *</label>
                <input v-model="form.applicantPhone" type="tel" required placeholder="请输入手机号">
              </div>
              <div class="form-group">
                <label>身份证号 *</label>
                <input v-model="form.applicantIdCard" type="text" required placeholder="请输入身份证号">
              </div>
              <div class="form-group">
                <label>邮箱</label>
                <input v-model="form.applicantEmail" type="email" placeholder="请输入邮箱地址">
              </div>
            </div>
          </div>

          <!-- 店铺信息 -->
          <div class="form-section">
            <h3>店铺信息</h3>
            <div class="form-subgrid">
              <div class="form-group">
                <label>店铺名称 *</label>
                <input v-model="form.storeName" type="text" required placeholder="请输入店铺名称">
              </div>
              <div class="form-group">
                <label>地址 *</label>
                <input v-model="form.storeAddress" type="text" required placeholder="请输入店铺地址">
              </div>
              <div class="form-group">
                <label>预期开业时间</label>
                <input v-model="form.openingDate" type="date">
              </div>
              <div class="form-group">
                <label>投资预算（万元）</label>
                <input v-model="form.budget" type="number" placeholder="例如：50">
              </div>
            </div>
          </div>
        </div>

        <div class="form-submit">
          <button type="submit">提交申请</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {
        applicantName: '',
        applicantPhone: '',
        applicantIdCard: '',
        applicantEmail: '',
        storeName: '',
        storeAddress: '',
        openingDate: '',
        budget: ''
      }
    };
  },
  methods: {
    submitApplication() {
      alert("申请已提交！我们会尽快与您联系。");
    }
  }
};
</script>

<style scoped>
.franchise-application {
  max-width: 1600px;
  margin: auto;
  padding: 50px 60px;
  font-family: Arial, sans-serif;
  background-color: #fff;
}

.header {
  text-align: center;
  margin-bottom: 50px;
}

.header h1 {
  margin-bottom: 10px;
  font-size: 36px;
}

.form-container form {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.form-grid-4col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px 120px;
}

.form-subgrid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px 40px;
}

.form-section h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 22px;
  grid-column: span 2;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 8px;
  font-weight: bold;
  font-size: 15px;
}

.form-group input {
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 16px;
}

.form-submit {
  text-align: center;
}

.form-submit button {
  background-color: #007bff;
  color: #fff;
  padding: 14px 60px;
  border: none;
  border-radius: 6px;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.form-submit button:hover {
  background-color: #0056b3;
}

@media (max-width: 768px) {
  .form-grid-4col,
  .form-subgrid {
    grid-template-columns: 1fr;
  }
}
</style>