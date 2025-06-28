<template>
    <div class="container">
      <!-- Sidebar -->
      <div class="sidebar">
        <h3>前台管理系统</h3>
        <ul>
            <li><router-link to="/counter">🏠 回到管理主页</router-link></li>
            <li><router-link to="/check-manager">👤 查看管理员</router-link></li> 
            <li><router-link to="/check-orders">💵 查看订单及结账</router-link></li>
            <li><router-link to="/manage-delivery">🛵 外卖员管理</router-link></li>
            <li><router-link to="/manage-tables">🪑 餐桌预定</router-link></li>
        </ul>
      </div>
  
  
      <!-- Content (main page content) -->
      <div class="content">
        <h2 class="page-title">🎟️ 分配优惠券</h2>
        <form @submit.prevent="submitForm" class="coupon-form">
          <div class="form-group">
            <label>用户 ID（填 ALL 分发全部）:</label>
            <input v-model="form.userId" placeholder="如：1 或 ALL" required />
          </div>
  
          <div class="form-group">
            <label>开始时间:</label>
            <input type="datetime-local" v-model="form.startDate" required />
          </div>
  
          <div class="form-group">
            <label>结束时间:</label>
            <input type="datetime-local" v-model="form.endDate" required />
          </div>
  
          <div class="form-group">
            <label>满减门槛（元）:</label>
            <input type="number" v-model="form.minThreshold" required />
          </div>
  
          <div class="form-group">
            <label>优惠金额（元）:</label>
            <input type="number" v-model="form.discountAmount" required />
          </div>
  
          <button type="submit">🚀 分发优惠券</button>
        </form>
  
        <div v-if="message" :class="{'success': success, 'error': !success}" class="message">
          {{ message }}
        </div>
      </div>
    </div>
  </template>
  
  <script>
import { ref } from 'vue';
import axios from 'axios';

export default {
  name: 'DistributeCoupons',
  setup() {
    const form = ref({
      userId: '',
      startDate: '',
      endDate: '',
      minThreshold: '',
      discountAmount: ''
    });

    const message = ref('');
    const success = ref(false);

    const submitForm = async () => {
      // ✅ 时间有效性判断
      const start = new Date(form.value.startDate);
      const end = new Date(form.value.endDate);
      if (start > end) {
        success.value = false;
        message.value = '开始时间不能晚于结束时间！';
        return;
      }

      try {
        const response = await axios.post('/api/coupons/distribute', form.value);
        success.value = response.data.success;
        message.value = response.data.message;
      } catch (error) {
        success.value = false;
        message.value = '请求出错，请稍后再试。';
        console.error(error);
      }
    };

    return { form, message, success, submitForm };
  }
};
</script>

  
  <style scoped>
   body {
    margin: 0;
    padding: 0;
    font-family: 'Arial', sans-serif;
    background: #f7f9fc;
    color: #333;
    display: flex;
  }
  
  .container {
    display: flex;
    max-width: 1200px;
    margin: 40px auto;
    padding: 40px;
    background-color: #fff;
    border-radius: 10px;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  }
  
  .sidebar {
    background-color: rgba(255, 255, 255, 0.8);
    padding: 20px;
    width: 200px;
    border-radius: 10px;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  }
  
  .sidebar h3 {
    margin-bottom: 10px;
    font-size: 20px;
    color: #2980b9;
    border-bottom: 2px solid #2980b9;
    padding-bottom: 10px;
  }
  
  .sidebar ul {
    list-style-type: none;
    padding: 0;
    margin: 0;
  }
  
  .sidebar ul li {
    margin-bottom: 10px;
  }
  
  .sidebar ul li a {
    text-decoration: none;
    color: #333;
    font-size: 16px;
    transition: color 0.3s ease;
  }
  
  .sidebar ul li a:hover {
    color: #2980b9;
    font-weight: bold;
  }
  
  .content {
    flex-grow: 1;
    padding: 2rem;
    width:760px;
    background-color: #fafafa;
    margin-top: 50px;
  }
  
  .page-title {
    text-align: center;
    font-size: 1.8rem;
    color: #2980b9;
  }
  
  .coupon-form {
    max-width: 600px;
    margin: 0 auto;
    padding: 2rem;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 0 10px #ccc;
  }
  
  .coupon-form .form-group {
    margin-bottom: 1rem;
  }
  
  .coupon-form label {
    font-weight: bold;
    display: block;
    margin-bottom: 0.3rem;
  }
  
  .coupon-form input {
    width: 100%;
    padding: 0.5rem;
    border-radius: 6px;
    border: 1px solid #ccc;
  }
  
  .coupon-form button {
    width: 100%;
    padding: 0.8rem;
    background-color: #28a745;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 1rem;
    cursor: pointer;
  }
  
  .coupon-form button:hover {
    background-color: #218838;
  }
  
  .message {
    margin-top: 1rem;
    padding: 0.7rem;
    border-radius: 6px;
    text-align: center;
  }
  
  .success {
    background-color: #d4edda;
    color: #155724;
  }
  
  .error {
    background-color: #f8d7da;
    color: #721c24;
  }
  </style>
  