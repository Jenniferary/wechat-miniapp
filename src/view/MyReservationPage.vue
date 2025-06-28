<template>
    <div class="container">
      <div class="order-table">
        <h2>我的预定餐桌</h2>
  
        <table v-if="reservations.length > 0">
          <thead>
            <tr>
              <th>桌位号</th>
              <th>预定时间</th>
              <th>预定ID</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in reservations" :key="item.reservationId">
              <td>{{ item.tableNumber }}</td>
              <td>{{ item.reservationTime }}</td>
              <td>{{ item.reservationId }}</td>
              <td>
                <button class="cancel-button" @click="cancelReservation(item.reservationId)">取消预约</button>
              </td>
            </tr>
          </tbody>
        </table>
  
        <div v-else class="no-reservation-box">
          <div class="no-reservation-icon">🍽️</div>
          <div class="no-reservation-text">您目前还没有预定餐桌</div>
          <div class="no-reservation-subtext">看起来您还未用餐，赶快预定一张专属座位吧！</div>
          <router-link to="/menu" class="go-to-menu">👉 返回菜单</router-link>
        </div>
  
        <p v-if="successMessage" class="success-message">{{ successMessage }}</p>
        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: 'MyReservationPage',
    data() {
      return {
        reservations: [],
        successMessage: '',
        errorMessage: ''
      }
    },
    mounted() {
      const username = localStorage.getItem('username');
      if (!username) {
        this.$router.push('/login');
        return;
      }
  
      fetch(`http://localhost:8080/api/my-reservation?username=${username}`)
        .then(res => res.json())
        .then(data => {
          this.reservations = data;
        })
        .catch(err => {
          this.errorMessage = '加载失败：' + err.message;
        });
    },
    methods: {
      async cancelReservation(reservationId) {
        this.successMessage = '';
        this.errorMessage = '';
  
        try {
          const res = await fetch('http://localhost:8080/api/cancel-reservation', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reservationId })
          });
  
          const result = await res.json();
  
          if (result.status === 'success') {
            this.successMessage = result.message;
            this.reservations = this.reservations.filter(r => r.reservationId !== reservationId);
          } else {
            this.errorMessage = result.message || '取消失败';
          }
        } catch (error) {
          this.errorMessage = '请求错误：' + error.message;
        }
      }
    }
  }
  </script>
  
  <style scoped>
  /* ✅ 背景干净纹理，不包含错位图像 */
  .container {
    display: flex;
    justify-content: center;
    align-items: flex-start;
    min-height: 100vh;
    padding: 40px 10px;
    box-sizing: border-box;
  
   /* ✅ 建议替换成干净背景图 */
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    background-attachment: scroll;
  }
  
  /* ✅ 放大内容卡片 + 毛玻璃 + 半透明 */
  .order-table {
    margin-top: 100px;
    background-color: rgba(255, 255, 255, 0.6);  
    backdrop-filter: blur(12px);              
    -webkit-backdrop-filter: blur(12px);
    border-radius: 20px;
    padding: 50px;
    width: 90%;
    max-width: 950px;  /* ✅ 放大显示 */
    text-align: center;
    box-shadow: 0 8px 40px rgba(0, 0, 0, 0.25);
  }
  
  table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 20px;
  }
  
  th, td {
    padding: 14px;
    border: 1px solid #ccc;
  }
  
  th {
    background-color: rgba(245, 245, 245, 0.8);
    font-weight: bold;
    font-size: 16px;
  }
  
  .cancel-button {
    padding: 6px 14px;
    background-color: #e74c3c;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
  }
  .cancel-button:hover {
    background-color: #c0392b;
  }
  
  .no-reservation-box {
    padding: 40px 10px;
    font-size: 18px;
    color: #444;
    text-align: center;
  }
  
  .no-reservation-icon {
    font-size: 48px;
    margin-bottom: 10px;
  }
  
  .no-reservation-text {
    font-size: 24px;
    font-weight: bold;
  }
  
  .no-reservation-subtext {
    margin-top: 10px;
    font-size: 16px;
    color: #666;
  }
  
  .go-to-menu {
    margin-top: 20px;
    display: inline-block;
    background-color: #3498db;
    color: white;
    text-decoration: none;
    padding: 12px 26px;
    border-radius: 6px;
    font-size: 18px;
    transition: background-color 0.3s ease;
  }
  
  .go-to-menu:hover {
    background-color: #2980b9;
  }
  
  .success-message {
    color: green;
    font-size: 16px;
    margin-top: 12px;
  }
  
  .error-message {
    color: red;
    font-size: 16px;
    margin-top: 12px;
  }
  </style>
  