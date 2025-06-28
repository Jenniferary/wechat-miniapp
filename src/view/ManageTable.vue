<template>
  <div class="container">
    <!-- Sidebar -->
    <div class="sidebar">
      <h3>前台管理系统</h3>
      <ul>
        <li><router-link to="/check-orders">（1）管理堂食订单</router-link></li>
        <li><router-link to="/distribute-coupons">（2）管理优惠券</router-link></li>
        <li><router-link to="/manage-delivery">（3）管理外卖订单</router-link></li>
        <li><router-link to="/manage-tables">（4）管理餐桌</router-link></li>
        <li><router-link to="/dishes">（5）管理菜品</router-link></li>
        <li><router-link to="/data-analytics">（6）数据分析</router-link></li>
        <li><router-link to="/counter">回到管理主页</router-link></li>
      </ul>
    </div>

    <!-- Content (main page content) -->
    <div class="content">
      <div class="form-card">
        <h2>管理餐桌</h2>

        <!-- Toggle between the two actions: 分配餐桌 and 释放餐桌 -->
        <div class="button-group">
          <button @click="showDistribute = true" :class="{ active: showDistribute }">分配餐桌</button>
          <button @click="showDistribute = false" :class="{ active: !showDistribute }">释放餐桌</button>
        </div>

        <!-- 分配餐桌 Form -->
        <div v-if="showDistribute">
          <h3>分配餐桌</h3>
          <div class="form-group">
            <label for="userId">用户 ID</label>
            <input v-model="userId" type="number" placeholder="请输入用户编号" required>
          </div>

          <div class="form-group">
            <label>选择桌型</label>
            <div class="radio-group">
              <label>
                <input v-model="tableType" type="radio" value="normal"> 普通桌位
              </label>
              <label>
                <input v-model="tableType" type="radio" value="private"> 包厢
              </label>
            </div>
          </div>

          <div class="form-group">
            <label for="numOfPeople">用餐人数</label>
            <input v-model="numOfPeople" type="number" placeholder="例如：4" required>
          </div>

          <div class="form-group">
            <label for="reservationTime">用餐时间</label>
            <input v-model="reservationTime" type="datetime-local" :min="minTime" required>
          </div>

          <button @click="submitReservation">确认分配</button>

          <div v-if="successMessage" class="success-message">{{ successMessage }}</div>
          <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        </div>

        <!-- 释放餐桌 Form -->
        <div v-else>
          <h3>释放餐桌</h3>
          <div class="form-group">
            <label for="userId">用户ID：</label>
            <input v-model="userId" type="number" required>
          </div>

          <button @click="releaseTable">确认释放</button>

          <div v-if="successMessage" class="success-message">{{ successMessage }}</div>
          <div v-if="releasedTables.length" class="released-tables">
            <div>已释放的桌位号：</div>
            <ul>
              <li v-for="table in releasedTables" :key="table">{{ table }}</li>
            </ul>
          </div>
          <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      showDistribute: true,
      userId: '',
      tableType: 'normal',
      numOfPeople: '',
      reservationTime: '',
      minTime: '',
      successMessage: '',
      errorMessage: '',
      releasedTables: [],
    };
  },
  created() {
    const now = new Date();
    const offset = -now.getTimezoneOffset();
    const localISOTime = new Date(now.getTime() + offset * 60000)
      .toISOString()
      .slice(0, 16);
    this.minTime = localISOTime;
  },
  methods: {
    async submitReservation() {
      this.successMessage = '';
      this.errorMessage = '';
      try {
        const res = await axios.post('http://localhost:8080/api/table/distribute', {
          userId: this.userId,
          tableType: this.tableType,
          numOfPeople: this.numOfPeople,
          reservationTime: this.reservationTime,
        });

        if (res.data.success) {
          this.successMessage = `餐桌分配成功！分配给用户ID：${this.userId}，桌号为：${res.data.tableNumber}`;
          setTimeout(() => { this.$router.push('/counter'); }, 3000);
        } else {
          this.errorMessage = res.data.message;
        }
      } catch (err) {
        this.errorMessage = '请求失败：' + err.message;
      }
    },

    async releaseTable() {
      this.successMessage = '';
      this.errorMessage = '';
      this.releasedTables = [];

      try {
        const response = await axios.post('http://localhost:8080/api/table/relieve', {
          userId: this.userId,
        });

        if (response.data.success) {
          this.successMessage = `餐桌释放成功！释放用户ID：${this.userId}`;
          this.releasedTables = response.data.releasedTables;
          setTimeout(() => { this.$router.push('/counter'); }, 3000);
        } else {
          this.errorMessage = response.data.message || '释放失败';
        }
      } catch (err) {
        this.errorMessage = '请求失败：' + err.message;
      }
    },
  },
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
    width: 1200px;
    margin: 40px auto;
    padding: 40px;
    background-color: #fff;
    border-radius: 10px;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  }
  
  .sidebar {
    background-color: rgba(255, 255, 255, 0.9);
    padding: 20px;
    width: 250px;
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
    width: 780px;
    background-color: #fafafa;
  }
  
  .form-card {
    background-color: rgba(255, 255, 255, 0.95);
    padding: 30px 50px;
    border-radius: 16px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 700px;
    margin: 0 auto;
  }

  h2 {
    text-align: center;
    margin-bottom: 20px;
    color: #2c3e50;
    font-size: 36px;
  }

  .button-group {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
    justify-content: center;
  }

  .button-group button {
    width: 48%;
    padding: 14px;
    background-color: #3498db;
    color: white;
    font-size: 18px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.3s ease;
  }

  .button-group button:hover {
    background-color: #2980b9;
  }

  .button-group .active {
    background-color: #2980b9;
  }

  .form-group {
    margin-bottom: 20px;
  }

  .form-group label {
    display: block;
    font-weight: bold;
    margin-bottom: 8px;
    color: #333;
  }

  .form-group input[type="number"],
  .form-group input[type="datetime-local"] {
    width: 100%;
    padding: 12px;
    border: 1px solid #ccc;
    border-radius: 8px;
    font-size: 16px;
  }

  .success-message {
    background-color: #2ecc71;
    color: white;
    padding: 10px;
    border-radius: 8px;
    margin-top: 20px;
    text-align: center;
  }

  .error-message {
    background-color: #e74c3c;
    color: white;
    padding: 10px;
    border-radius: 8px;
    margin-top: 20px;
    text-align: center;
  }

  .released-tables {
    margin-top: 20px;
  }

  .released-tables ul {
    padding: 0;
    margin: 0;
    list-style-type: none;
  }
</style>
