<template>
  <div class="container">
    <div class="sidebar">
      <h3>选择</h3>
      <ul>
        <li><router-link to="/my-account">个人主页</router-link></li>
        <li><router-link to="/menu">菜单</router-link></li>
        <li><router-link to="/coupon">优惠劵</router-link></li>
        <li><router-link to="/ordertable">订桌</router-link></li>
        <li><router-link to="/package">套餐</router-link></li>
        <li><router-link to="/orders">历史订单</router-link></li>
        <li><router-link to="/takeaway-order">外送订单</router-link></li>
        <li><router-link to="/reviews">菜品评价</router-link></li>
        <li><router-link to="/exchange">积分兑换</router-link></li>
      </ul>
    </div>

    <div class="order-container">
      <h2>我的外卖订单</h2>
      <div v-if="orders.length" class="orders-list">
        <div v-for="order in orders" :key="order.orderId" class="order-item">
          <div class="order-info">
            <p>订单ID: {{ order.orderId }}</p>
            <p>时间: {{ formatDate(order.timeOrdered) }}</p>
          </div>

          <div class="order-details">
            <p>送达地址: {{ order.deliveryAddress }}</p>
            <p>菜品: {{ order.dishList }}</p>
          </div>

          <div class="order-total">
            总价格: ￥{{ order.price }}
            <p v-if="order.isCouponUsed">
              使用了优惠券，优惠金额: ￥{{ order.discountAmount }}
            </p>
            <p v-else>
              未使用优惠券
            </p>
          </div>

          <div class="payment-status">
            <p>预计送达时间：{{ formatDate(order.estimatedDeliveryTime) }}</p>

            <p v-if="order.deliveryPersonId !== 0">
              骑手{{ order.deliveryPersonId }}正在全力配送，请耐心等待
            </p>

            <p v-if="order.deliveryStatus === 'delivered'">
              {{ formatDate(order.deliveryTime) }}订单已送达，祝您用餐愉快
            </p>
            <p v-else-if="order.deliveryStatus === 'out_for_delivery'">
              订单派送中
            </p>
            <p v-else-if="order.deliveryStatus === 'pending'">
              订单等待处理
            </p>
          </div>

          <div class="action-btn">
            <form
              v-if="order.deliveryStatus !== 'delivered'"
              @submit.prevent="confirmDelivery(order.orderId)"
            >
              <input type="hidden" :value="order.orderId" name="orderId">
              <button type="submit">
                确认收货并结账
              </button>
            </form>
          </div>
        </div>
      </div>
      <div v-else>
        <p>暂无订单记录</p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      orders: [],
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
        const res = await axios.get(`http://localhost:8080/api/takeaway/orders/${username}`);
        this.orders = res.data || [];
      } catch (err) {
        console.error("加载外卖订单失败", err);
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
    async confirmDelivery(orderId) {
      try {
        const res = await axios.post("http://localhost:8080/api/takeaway/confirm", { orderId });
        if (res.data.success) {
          alert("确认收货成功");
          this.fetchOrders();
        } else {
          alert("确认失败: " + res.data.message);
        }
      } catch (err) {
        alert("网络错误");
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

.order-details {
  background-color: #f0f4ff;
  padding: 12px 16px;
  border-radius: 8px;
  margin: 15px 0;
  font-size: 15px; 
  font-family: 'Georgia', serif;
  color: #2c3e50;
  font-weight: bold;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.1);
  word-wrap: break-word;
  line-height: 1.6;
  text-align: left;
}

.order-details p {
  font-size: 15px !important;
  margin: 4px 0;
}



.payment-status {
  margin-top: 10px;
  font-size: 16px;
  color: #555;
  text-align: left;
}

.payment-status p {
  margin: 4px 0;
  font-size: 15px !important;
}


.action-btn {
  text-align: center;
  margin-top: 20px;
}

button {
  padding: 12px 28px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.3s;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

button:hover {
  background-color: #2980b9;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 支付方式弹窗样式 */
.payment-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

/* 弹窗内容 */
.dialog-content {
  background-color: #fff;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  text-align: center;
  width: 90%;
  max-width: 400px;
}

.dialog-content h3 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

/* 支付方式弹窗样式 */
.payment-dialog {
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5); /* 半透明背景 */
  z-index: 1000;
}

/* 弹窗内容 */
.dialog-content {
  background-color: white;
  padding: 20px;
  border-radius: 10px;
  width: 300px; /* 设置弹窗宽度 */
  display: flex;
  flex-direction: column;
  align-items: center; /* 按钮居中对齐 */
  gap: 5px; /* 按钮间距 */
}

/* 按钮样式 */
.dialog-content button {
  padding: 12px 25px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  width: 100%; /* 按钮宽度为100% */
  max-width: 180px; /* 按钮最大宽度 */
}

.dialog-content button:hover {
  background-color: #2980b9;
}

.dialog-content button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 支付方式按钮图标 */
.payment-button button i {
  margin-right: 10px;
  font-size: 20px;
}

/* 关闭按钮 */
.dialog-close-btn {
  margin-top: 15px;
  padding: 12px 20px;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.dialog-close-btn:hover {
  background-color: #c0392b;
}

@media (max-width: 768px) {
  .container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    height: auto;
    margin-bottom: 20px;
  }

  .order-container {
    max-width: 100%;
  }
}
  </style>
  