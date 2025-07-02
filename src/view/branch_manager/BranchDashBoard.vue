<template>
  <div class="dashboard-page">
    <div class="sidebar">
      <h2>📌 店长管理</h2>
      <ul>
        <li @click="$router.push('/branch-dashboard')"><strong>店铺概况</strong></li>
        <li @click="$router.push('/branch-orders')">订单管理</li>
        <li @click="$router.push('/branch-employee')">员工入职审批</li> 
        <li @click="$router.push('/branch-leaving')">员工请假审批</li> 
        <li @click="$router.push('/branch-leavingworking-review')">员工离职审批</li>
        <li @click="$router.push('/branch-employee')">员工打卡情况总览</li> 
        <li @click="$router.push('/branch-feedback')">客户反馈</li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <h3>店铺概况</h3>

      <div class="store-info">
        <div class="info-card">
          <h4>门店名称</h4>
          <p>{{ storeInfo.name }}</p>
        </div>

        <div class="info-card">
          <h4>总订单数</h4>
          <p>{{ storeInfo.totalOrders }}</p>
        </div>

        <div class="info-card">
          <h4>员工总数</h4>
          <p>{{ storeInfo.totalEmployees }}</p>
        </div>

        <div class="info-card">
          <h4>今日收入</h4>
          <p>{{ storeInfo.todayRevenue }}</p>
        </div>
      </div>

      <h3>待处理订单</h3>
      <table v-if="orders.length">
        <thead>
          <tr>
            <th>订单号</th>
            <th>顾客姓名</th>
            <th>订单金额</th>
            <th>订单状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.orderId">
            <td>{{ order.orderId }}</td>
            <td>{{ order.customerName }}</td>
            <td>{{ order.amount }}</td>
            <td>
              <span :class="['status', statusClass(order.status)]">{{ translateStatus(order.status) }}</span>
            </td>
            <td>
              <button v-if="order.status === '待处理'" @click="processOrder(order.orderId)" class="btn-process">处理</button>
            </td>
          </tr>
        </tbody>
      </table>

      <p v-else>暂无待处理的订单</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "BranchDashBoard",
  data() {
    return {
      storeInfo: {},
      orders: [],
    };
  },
  created() {
    this.fetchStoreInfo();
    this.fetchPendingOrders();
  },
  methods: {
    async fetchStoreInfo() {
      try {
        const res = await fetch("/api/branch/info");
        const json = await res.json();
        if (json.status === "success") {
          this.storeInfo = json.data;
        }
      } catch (err) {
        console.error("加载店铺信息失败", err);
      }
    },
    async fetchPendingOrders() {
      try {
        const branchId = localStorage.getItem("branchId");
        if (!branchId) throw new Error("未登录");

        const res = await fetch(`/api/orders/pending?branchId=${branchId}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const json = await res.json();
        this.orders = json;
      } catch (err) {
        alert("加载待处理订单失败：" + err.message);
      }
    },
    translateStatus(status) {
      switch (status) {
        case "待处理": return "待处理";
        case "处理中": return "处理中";
        case "已完成": return "已完成";
        case "已取消": return "已取消";
        default: return "未知";
      }
    },
    statusClass(status) {
      switch (status) {
        case "待处理": return "pending";
        case "处理中": return "in-progress";
        case "已完成": return "completed";
        case "已取消": return "cancelled";
        default: return "unknown";
      }
    },
    async processOrder(orderId) {
      try {
        const res = await fetch(`/api/orders/${orderId}/process`, { method: "PUT" });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        alert("订单已处理");
        this.fetchPendingOrders();
      } catch (err) {
        alert("处理订单失败：" + err.message);
      }
    },
    logout() {
      localStorage.clear();
      this.$router.push("/login");
    }
  },
};
</script>

<style scoped>
.dashboard-page {
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
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.sidebar h2 {
  margin-bottom: 30px;
  font-size: 22px;
  border-bottom: 2px solid #fff;
  padding-bottom: 10px;
}

.sidebar ul {
  list-style: none;
  padding-left: 0;
  margin: 0;
  flex: 1;
}

.sidebar li {
  padding: 10px 0;
  font-size: 15px;
  cursor: pointer;
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
  width: calc(100vw - 220px);
  background: white;
  padding: 40px 60px;
  box-sizing: border-box;
  overflow-y: auto;
}

.store-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.info-card {
  width: calc(25% - 20px);
  background: #f1f1f1;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
}

.info-card h4 {
  font-size: 18px;
  color: #333;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 0 12px rgba(0, 0, 0, 0.05);
}

th, td {
  padding: 14px 24px;
  text-align: center;
  border-bottom: 1px solid #eee;
  font-size: 16px;
  color: #555;
}

th {
  background-color: #007bff;
  color: white;
  font-weight: 600;
}

tr:hover {
  background-color: #f1f7ff;
}

p {
  font-size: 18px;
  margin-top: 30px;
  color: #888;
  text-align: center;
}

.status {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 20px;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.status.pending {
  background-color: #f39c12;
}

.status.in-progress {
  background-color: #2980b9;
}

.status.completed {
  background-color: #27ae60;
}

.status.cancelled {
  background-color: #c0392b;
}

.status.unknown {
  background-color: #7f8c8d;
}

.btn-process {
  background-color: #27ae60;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-process:hover {
  background-color: #219150;
}
</style>
