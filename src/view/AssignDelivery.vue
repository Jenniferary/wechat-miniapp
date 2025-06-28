<template>
  <div class="container">
    <!-- Sidebar -->
    <div class="sidebar">
      <h3>前台管理系统</h3>
      <ul>
        <li>
          <router-link to="/counter">
            🏠 回到管理主页
          </router-link>
        </li>
        <li><router-link to="/look-manager">
          👤 查看管理员
        </router-link></li> 
        <li>
          <router-link to="/check-orders">
            💵 查看订单及结账
          </router-link>
        </li>
        <li>
          <router-link to="/distribute-coupons">
            🎟️ 分配优惠券
          </router-link>
        </li>
        <li>
          <router-link to="/manage-tables">
            🪑 餐桌预定
          </router-link>
        </li>
      </ul>
    </div>
  
    <!-- Main Content -->
    <div class="content">
      <h2 class="page-title">
        📦 分配外卖员
      </h2>
  
      <div
        v-if="orders.length > 0"
        class="order-list"
      >
        <div
          v-for="order in orders"
          :key="order.orderId"
          class="order-card"
        >
          <div class="order-info">
            <p><strong>订单ID：</strong>{{ order.orderId }}</p>
            <p><strong>送达地址：</strong>{{ order.deliveryAddress }}</p>
            <p><strong>下单时间：</strong>{{ order.timeOrdered }}</p>
            <p><strong>菜品：</strong>{{ order.dishList }}</p>
            <p><strong>总价格：</strong>￥{{ order.price }}</p>
            <p><strong>优惠金额：</strong>￥{{ order.discountAmount }}</p>
          </div>
  
          <div class="assign-form">
            <select v-model="selectedDeliveryPerson[order.orderId]">
              <option
                disabled
                value=""
              >
                请选择外卖员
              </option>
              <option
                v-for="person in deliveryPersons"
                :key="person.deliveryPersonId"
                :value="person.deliveryPersonId"
              >
                外卖员{{ person.deliveryPersonId }} - {{ person.name }}
              </option>
            </select>
            <button @click="assign(order.orderId)">
              🚚 分配
            </button>
          </div>
        </div>
      </div>
  
      <div
        v-else
        class="no-orders"
      >
        暂无待分配的外卖订单。
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
        deliveryPersons: [],
        selectedDeliveryPerson: {},
      };
    },
    created() {
      this.fetchOrders();
      this.fetchDeliveryPersons();
    },
    methods: {
      async fetchOrders() {
        try {
          const res = await axios.get("http://localhost:8080/api/takeaway/pending-orders");
          this.orders = res.data || [];
        } catch (err) {
          console.error("获取订单失败", err);
        }
      },
      async fetchDeliveryPersons() {
        try {
          const res = await axios.get("http://localhost:8080/api/takeaway/available-delivery-persons");
          this.deliveryPersons = res.data || [];
        } catch (err) {
          console.error("获取外卖员失败", err);
        }
      },
      async assign(orderId) {
        const personId = this.selectedDeliveryPerson[orderId];
        if (!personId) {
          alert("请选择要分配的外卖员");
          return;
        }
  
        try {
          const res = await axios.post("http://localhost:8080/api/takeaway/assign-delivery", {
            orderId,
            deliveryPersonId: personId,
          });
          if (res.data.success) {
            alert("分配外卖员成功！");
            this.fetchOrders();
          } else {
            alert("分配失败：" + res.data.message);
          }
        } catch (err) {
          console.error("分配失败", err);
          alert("分配请求失败：" + err.message);
        }
      },
    },
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
  .content {
    flex-grow: 1;
    padding: 2rem;
    width: 760px;
    background-color: #fafafa;
  }
  .page-title {
    text-align: center;
    font-size: 1.8rem;
    color: #2980b9;
    margin-bottom: 1.5rem;
  }
  .order-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  .order-card {
    background: #ffffff;
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 0 10px #ccc;
  }
  .order-info p {
    margin: 6px 0;
    font-size: 16px;
  }
  .assign-form {
    display: flex;
    gap: 10px;
    margin-top: 15px;
  }
  .assign-form select {
    flex-grow: 1;
    padding: 8px;
    border-radius: 6px;
    border: 1px solid #ccc;
  }
  .assign-form button {
    padding: 10px 18px;
    background-color: #28a745;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 1rem;
    cursor: pointer;
  }
  .assign-form button:hover {
    background-color: #218838;
  }
  .no-orders {
    text-align: center;
    margin-top: 40px;
    font-size: 1.2rem;
    color: #999;
  }
  </style>