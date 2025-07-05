<template>
  <div class="resume-page">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <h2>💁‍♀️ 前台管理系统</h2>
      <ul class="menu-list">
        <li @click="$router.push('/counter-dashboard')">个人档案</li>
        <li @click="$router.push('/counter-dinein-order')">管理堂食订单</li>
        <li @click="$router.push('/manage-tables')">管理餐桌</li>

        <li>
          <strong style="margin-top: 20px; cursor: pointer; color: #fff; font-weight: bold;">
            外卖管理
          </strong>
        </li>
        <li @click="$router.push('/delivery-assign')">分配外卖员</li>
        <li @click="$router.push('/delivery-add')">添加外卖员</li>
        <li class="active">查看外卖订单</li>

        <li @click="$router.push('/counter-attendance')">考勤打卡</li>
        <li @click="$router.push('/counter-leave')">请假申请</li>
        <li @click="$router.push('/counter-leave-progress')">我的请假记录</li>
      </ul>
      <div class="logout" @click="logout">退出系统</div>
    </div>

    <!-- 内容区域 -->
    <div class="form-section">
      <h3>查看外卖订单</h3>

      <div v-if="orders.length" class="orders-list">
        <div v-for="order in orders" :key="order.orderId" class="order-card">
          <div class="order-header">
            <div>
              <p><strong>订单ID：</strong>{{ order.orderId }}</p>
              <p><strong>用户ID：</strong>{{ order.userId }}</p>
              <p><strong>送达地址：</strong>{{ order.deliveryAddress }}</p>
              <p><strong>下单时间：</strong>{{ formatDate(order.timeOrdered) }}</p>
            </div>
            <div class="price-info">
              <p><strong>总价格：</strong>￥{{ order.price.toFixed(2) }}</p>
            </div>
          </div>

          <div class="dish-info">
            <strong>菜品：</strong>{{ order.dishList }}
          </div>

          <div class="delivery-info">
            <p><strong>配送状态：</strong>{{ statusText(order.deliveryStatus) }}</p>
            <p v-if="order.discountAmount"><strong>优惠金额：</strong>￥{{ order.discountAmount }}</p>
            <p v-if="order.deliveryPersonId"><strong>外卖员ID：</strong>{{ order.deliveryPersonId }}</p>
          </div>
        </div>
      </div>

      <div v-else class="no-data">
        暂无外卖订单记录。
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
    };
  },
  created() {
    this.fetchOrders();
  },
  methods: {
    async fetchOrders() {
      try {
        const res = await axios.get("http://localhost:8080/api/takeaway/all-orders");
        this.orders = res.data || [];
      } catch (err) {
        console.error("获取外卖订单失败", err);
      }
    },
    formatDate(dateStr) {
      const date = new Date(dateStr);
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    },
    statusText(status) {
      const map = {
        pending: '待分配',
        delivering: '配送中',
        delivered: '已送达'
      };
      return map[status] || '未知';
    },
    logout() {
      localStorage.removeItem("counterId");
      window.location.href = "/login";
    }
  }
};
</script>

<style scoped>
.resume-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}
.sidebar {
  width: 240px;
  background: #1d3557;
  color: white;
  padding: 30px 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.sidebar h2 {
  margin-bottom: 30px;
  font-size: 22px;
  border-bottom: 2px solid #fff;
  padding-bottom: 10px;
}
.menu-list {
  flex: 1;
  list-style: none;
  padding-left: 0;
  margin: 0;
  overflow-y: auto;
}
.menu-list li {
  padding: 10px 0;
  font-size: 15px;
  cursor: pointer;
  color: #ccc;
  user-select: none;
}
.menu-list li.active {
  color: #00b4d8;
  font-weight: bold;
}
.logout {
  color: #ffb3b3;
  transition: color 0.3s ease;
}
.logout:hover {
  color: #ffffff;
  font-weight: bold;
}

.form-section {
  flex: 1;
  padding: 40px 60px;
  background: #fff;
  overflow-y: auto;
  border-radius: 12px;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
  max-height: 100vh;
  display: flex;
  flex-direction: column;
}

.form-section h3 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 36px;
  border-left: 6px solid #007bff;
  padding-left: 16px;
  color: #333;
  letter-spacing: 0.04em;
  user-select: none;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  overflow-y: auto;
  padding-right: 8px;
}

.order-card {
  background: #fefefe;
  border-radius: 16px;
  padding: 28px 32px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: 1px solid #e3e8f9;
}

.order-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #e0e7ff;
  padding-bottom: 20px;
  margin-bottom: 20px;
}

.order-header > div p {
  margin: 6px 0;
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
}

.order-header > div p strong {
  font-weight: 700;
  color: #1d3557;
}

.price-info p {
  font-size: 22px;
  font-weight: 700;
  color: #007bff;
  margin: 0;
  text-align: right;
}

.dish-info {
  font-weight: 600;
  font-size: 17px;
  background-color: #e9f0ff;
  padding: 16px 20px;
  border-radius: 14px;
  color: #1d3557;
  margin-bottom: 20px;
  line-height: 1.5;
  letter-spacing: 0.02em;
  user-select: text;
  white-space: pre-wrap;
  word-break: break-word;
}

.delivery-info p {
  margin: 8px 0;
  font-size: 15px;
  color: #556677;
}

.delivery-info p strong {
  font-weight: 700;
  color: #3a4a6d;
}

/* 无数据样式 */
.no-data {
  margin-top: 80px;
  text-align: center;
  font-size: 18px;
  color: #9aa5b1;
  font-weight: 600;
}

</style>
