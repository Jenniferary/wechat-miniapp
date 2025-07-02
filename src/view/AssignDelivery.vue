<template>
  <div class="resume-page">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <h2>💁‍♀️ 前台管理系统</h2>
      <ul class="menu-list">
        <li @click="$router.push('/counter-dashboard')"><strong>个人档案</strong></li>
        <li @click="$router.push('/counter-dinein-order')">管理堂食订单</li>
        <li @click="$router.push('/manage-tables')">管理餐桌</li>

        <li>
          <strong style="margin-top: 20px; cursor: pointer; color: #fff; font-weight: bold;">
            外卖管理
          </strong>
        </li>
        <li class="active">分配外卖员</li>
        <li @click="$router.push('/delivery-add')" style="padding-left: 15px;">添加外卖员</li>
        <li @click="$router.push('/delivery-view')" style="padding-left: 15px;">查看外卖订单</li>

        <li @click="$router.push('/counter-attendance')">考勤打卡</li>
        <li @click="$router.push('/counter-leave')">请假申请</li>
        <li @click="$router.push('/counter-leave-progress')">我的请假记录</li>
      </ul>

      <div class="logout" @click="logout">退出系统</div>
    </div>

    <!-- 主内容 -->
    <div class="form-section">
      <h3>分配外卖员</h3>

      <div v-if="orders.length > 0" class="orders-list">
        <div v-for="order in orders" :key="order.orderId" class="order-item">
          <div class="order-header">
            <div class="order-info">
              <p><strong>订单ID：</strong>{{ order.orderId }}</p>
              <p><strong>送达地址：</strong>{{ order.deliveryAddress }}</p>
              <p><strong>下单时间：</strong>{{ order.timeOrdered }}</p>
              <p><strong>菜品：</strong>{{ order.dishList }}</p>
              <p><strong>总价格：</strong>￥{{ order.price }}</p>
              <p><strong>优惠金额：</strong>￥{{ order.discountAmount }}</p>
            </div>
          </div>

          <div class="assign-form">
            <select v-model="selectedDeliveryPerson[order.orderId]">
              <option disabled value="">请选择外卖员</option>
              <option
                v-for="person in deliveryPersons"
                :key="person.deliveryPersonId"
                :value="person.deliveryPersonId"
              >
                外卖员{{ person.deliveryPersonId }} - {{ person.name }}
              </option>
            </select>
            <button @click="assign(order.orderId)">分配</button>
          </div>
        </div>
      </div>

      <div v-else><p>暂无待分配的外卖订单。</p></div>
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
    logout() {
      localStorage.removeItem("counterId");
      this.$router.push("/login");
    },
  },
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
  background: white;
  padding: 40px 60px;
  box-sizing: border-box;
  overflow-y: auto;
}
.form-section h3 {
  font-size: 24px;
  margin-bottom: 30px;
  border-left: 6px solid #007bff;
  padding-left: 14px;
  color: #333;
}
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
}
.order-item {
  background-color: #ffffff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
}
.order-header {
  margin-bottom: 10px;
}
.order-info p {
  margin: 5px 0;
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
</style>
