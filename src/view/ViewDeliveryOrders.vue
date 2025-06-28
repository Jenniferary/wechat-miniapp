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
  
    <!-- Main Content -->
    <div class="order-container">
      <h2 class="page-title">
        查看外卖订单
      </h2>
  
      <div
        v-if="orders.length"
        class="orders-list"
      >
        <div
          v-for="order in orders"
          :key="order.orderId"
          class="order-item"
        >
          <div class="order-header">
            <div class="order-info">
              <p><strong>订单ID：</strong>{{ order.orderId }}</p>
              <p><strong>用户ID：</strong>{{ order.userId }}</p>
              <p><strong>送达地址：</strong>{{ order.deliveryAddress }}</p>
              <p><strong>下单时间：</strong>{{ formatDate(order.timeOrdered) }}</p>
            </div>
            <div class="order-total">
              <p><strong>总价格：</strong>￥{{ order.price.toFixed(2) }}</p>
            </div>
          </div>
  
          <div class="order-details">
            <p><strong>菜品：</strong>{{ order.dishList }}</p>
          </div>
  
          <div class="payment-status">
            <p><strong>配送状态：</strong>{{ statusText(order.deliveryStatus) }}</p>
            <p v-if="order.discountAmount">
              <strong>优惠金额：</strong>￥{{ order.discountAmount }}
            </p>
            <p v-if="order.deliveryPersonId">
              <strong>外卖员ID：</strong>{{ order.deliveryPersonId }}
            </p>
          </div>
        </div>
      </div>
      <div v-else>
        <p>暂无外卖订单记录</p>
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
      }
    }
  };
  </script>
  
  <style scoped>
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
    width: 900px;
    margin-left: 30px;
    padding: 20px;
    background-color: #ffffff;
    border-radius: 15px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
    font-size: 16px;
    flex: 1;
  }
  
  .page-title {
    text-align: center;
    font-size: 32px;
    color: #2c3e50;
    margin-bottom: 30px;
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
  
  .payment-status {
    margin-top: 10px;
    font-size: 16px;
    color: #555;
  }
  
  .payment-status p {
    margin: 4px 0;
  }
  </style>