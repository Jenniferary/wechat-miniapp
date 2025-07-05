<template>
  <div class="container">
    <!-- Sidebar -->
    <div class="sidebar">
      <h3>选择</h3>
      <ul>
        <li><router-link to="/my-account">个人主页</router-link></li>
        <li><router-link to="/menu">菜单</router-link></li>
        <li><router-link to="/coupon">优惠劵</router-link></li>
        <li><router-link to="/ordertable">订桌</router-link></li>
        <li><router-link to="/package">套餐</router-link></li>
        <li><router-link to="/takeaway-order">外送订单</router-link></li>
        <li><router-link to="/reviews">菜品评价</router-link></li>
        <li><router-link to="/exchange">积分兑换</router-link></li>
      </ul>
    </div>

    <!-- 订单容器 -->
    <div class="order-container">
      <h2>我的订单</h2>
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

          <div class="payment-status">
            <p>
              <strong>支付状态：</strong>
              <span :class="{ unpaid: !order.is_paid }">
                {{ order.is_paid? "✅ 已结账" : "❌ 未结账" }}
              </span>
            </p>
            <p v-if="order.is_coupon_used"><strong>使用优惠券：</strong>￥{{ order.discount_amount || 0 }}</p>
            <p v-else><strong>未使用优惠券</strong></p>
          </div>

          <div class="action-btn">
            <button v-if="!order.is_paid" @click="openPaymentDialog(order.order_id)">立即结账</button>
          </div>
        </div>
      </div>
      <div v-else>
        <p>暂无订单记录</p>
      </div>
    </div>

    <!-- 支付方式选择弹窗 -->
    <div v-if="isPaymentDialogVisible" class="payment-dialog">
      <div class="dialog-content">
        <h3>选择支付方式</h3>
        <button @click="payWithAlipay">支付宝</button>
        <button @click="payWithWeChat">微信</button>
        <button @click="payWithCard">储值卡</button>
        <button @click="closePaymentDialog">关闭</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      orders: [],
      isPaymentDialogVisible: false,
      currentOrderId: null,
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
        const response = await axios.get(`http://localhost:8080/api/order/my-orders?username=${username}`);
        if (response.data.success) {
          this.orders = response.data.orders;
        } else {
          alert("加载订单失败");
        }
      } catch (error) {
        console.error("获取订单失败", error);
        alert("网络错误，无法加载订单");
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

    openPaymentDialog(orderId) {
      this.currentOrderId = orderId;
      this.isPaymentDialogVisible = true;
    },

    closePaymentDialog() {
      this.isPaymentDialogVisible = false;
      this.currentOrderId = null;
    },

    async payWithAlipay() {
      this.processPayment('alipay');
    },

    async payWithWeChat() {
      this.processPayment('wechat');
    },

    async payWithCard() {
      this.processPayment('card');
    },

    async processPayment(paymentMethod) {
      try {
        const response = await axios.post("http://localhost:8080/api/order/checkout", {
          order_id: this.currentOrderId,
          payment_method: paymentMethod,
        });

        if (response.data.success) {
          this.closePaymentDialog();
          alert("支付成功");
          this.fetchOrders();  // 重新获取订单
        } else {
          alert("支付失败");
        }
      } catch (error) {
        console.error("支付失败", error);
        alert("支付过程中出现错误");
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

.order-details p {
  background-color: #f0f4ff;
  padding: 12px 16px;
  border-radius: 8px;
  margin: 15px 0;
  font-size: 18px; /* 调整字体大小 */
  font-family: 'Georgia', serif; /* 改变字体 */
  color: #2c3e50; /* 改变字体颜色 */
  font-weight: bold; /* 加粗字体 */
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.1); /* 添加文本阴影 */
  word-wrap: break-word; /* 防止长单词被截断 */
  line-height: 1.6; /* 调整行高，提升可读性 */
}


.payment-status {
  margin-top: 10px;
  font-size: 16px;
  color: #555;
}

.payment-status p {
  margin: 4px 0;
}

.payment-status .unpaid {
  color: #e74c3c;
  font-weight: bold;
}

.payment-status span:not(.unpaid) {
  color: #27ae60;
  font-weight: bold;
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
