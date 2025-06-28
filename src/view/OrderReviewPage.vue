<template>
    <div>
      <!-- 主容器 -->
      <div class="container">
        <!-- Sidebar -->
        <div class="sidebar">
          <h3>选择</h3>
          <ul>
            <li><router-link to="/my-account">用户主页</router-link></li>
            <li><router-link to="/menu">菜单</router-link></li>
          <li><router-link to="/coupon">优惠劵</router-link></li>
          <li><router-link to="/ordertable">订桌</router-link></li>
          <li><router-link to="/package">套餐</router-link></li>
          <li><router-link to="/orders">历史订单</router-link></li>
          <li><router-link to="/takeaway-order">外送订单</router-link></li>
          <li><router-link to="/exchange">积分兑换</router-link></li>
          </ul>
        </div>
  
        <!-- 内容区：订单卡片列表 -->
        <div class="order-container">
          <h2>我的评价</h2>
          <div v-if="orders.length" class="orders-list">
            <div v-for="order in orders" :key="order.order_id" class="order-item">
              <div class="order-header">
                <div class="order-info">
                  <p><strong>订单ID：</strong>{{ order.order_id }}</p>
                  <p><strong>桌号：</strong>{{ order.table_number }}</p>
                  <p><strong>时间：</strong>{{ formatDate(order.time_ordered) }}</p>
                </div>
                <div class="order-total">
                  <p><strong>总价格：</strong>￥{{ order.price.toFixed(2) }}</p>
                </div>
              </div>
  
              <div class="order-details">
                <p><strong>菜品：</strong>{{ order.dish_list }}</p>
              </div>
  
              <div class="review-section">
                <div v-if="!order.reviewed" class="review-form">
                  <label>评分：</label>
                  <select v-model="order.rating">
                    <option v-for="i in 5" :key="i" :value="i">{{ i }}</option>
                  </select>
                  <label>评论：</label>
                  <textarea v-model="order.comment" rows="3" />
                  <button @click="submitReview(order)">提交评价</button>
                </div>
                <div v-else class="reviewed-message">
                  <p><strong>您已经评价过该订单。</strong></p>
                  <p>评分：{{ order.rating }}</p>
                  <p>评论：{{ order.comment }}</p>
                </div>
              </div>
            </div>
          </div>
          <div v-else>
            <p>暂无订单记录</p>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import axios from "axios";
  
  export default {
    name: "MyReviewPage",
    data() {
      return {
        orders: []
      };
    },
    created() {
      this.fetchOrders();
    },
    methods: {
      async fetchOrders() {
        const username = localStorage.getItem("username");
        if (!username) {
          alert("请先登录");
          this.$router.push("/login");
          return;
        }
  
        try {
          const response = await axios.get(`http://localhost:8080/api/orders?username=${username}`);
          this.orders = response.data.map(o => ({
            ...o,
            rating: o.rating || 5,
            comment: o.comment || '',
          }));
        } catch (e) {
          alert("加载失败");
        }
      },
      formatDate(dateStr) {
      const date = new Date(dateStr);
      const year = date.getUTCFullYear();
      const month = (date.getUTCMonth() + 1).toString().padStart(2, '0');
      const day = date.getUTCDate().toString().padStart(2, '0');
      const hours = date.getUTCHours().toString().padStart(2, '0');
      const minutes = date.getUTCMinutes().toString().padStart(2, '0');
      const seconds = date.getUTCSeconds().toString().padStart(2, '0');

      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
      async submitReview(order) {
        try {
          await axios.post('http://localhost:8080/api/submit-review', {
            order_id: order.order_id,
            rating: order.rating,
            comment: order.comment
          });
          order.reviewed = true;
        } catch (e) {
          alert("提交失败");
        }
      }
    }
  };
  </script>
  
  <style scoped>
  body {
  margin: 0;
  padding: 0;
  font-family: 'Roboto', sans-serif;
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
  font-size: 22px;
  font-family: 'Playfair Display', serif;
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

.order-container {
  width: 900px; /* 固定宽度 */
  margin: 40px auto; /* 居中显示 */
  padding: 20px;
  background-color: #ffffff;
  border-radius: 15px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  font-size: 16px;
  flex: 1;
}

h2 {
  text-align: center;
  font-size: 32px;
  color: #2c3e50;
  margin-bottom: 40px;
}

.orders-list {
  display: flex;
  flex-direction: column;
}

.order-item {
  background-color: #ffffff;
  padding: 25px;
  margin-bottom: 25px;
  border-radius: 12px;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.order-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 15px;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 15px;
}

.order-info p,
.order-total p {
  margin: 5px 0;
  font-size: 16px;
  text-align: left;
}

.order-total p {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
}

.order-details p {
  background-color: #f0f4ff;
  padding: 12px 16px;
  border-radius: 8px;
  margin: 15px 0;
  font-size: 18px;
  font-family: 'Georgia', serif;
  color: #2c3e50;
  font-weight: bold;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.1);
  word-wrap: break-word;
  line-height: 1.6;
}

.review-section {
  margin-top: 10px;
  font-size: 16px;
}

.review-form textarea,
.review-form select {
  width: 100%;
  padding: 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
  margin-top: 5px;
}

.review-form button {
  margin-top: 10px;
  padding: 10px 20px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.review-form button:hover {
  background-color: #2980b9;
}

.reviewed-message {
  background-color: #f0f4ff;
  padding: 12px 16px;
  border-radius: 8px;
  margin: 15px 0;
  font-size: 18px;
  font-family: 'Georgia', serif;
  color: #2c3e50;
  font-weight: bold;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.1);
  word-wrap: break-word;
  line-height: 1.6;
  text-align: left; 
}

  </style>
  