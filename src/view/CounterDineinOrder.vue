<template>
    <div class="resume-page">
      <!-- 侧边栏 -->
      <div class="sidebar">
  <h2>💁‍♀️ 前台管理系统</h2>
  <ul class="menu-list">
    <li
      :class="{ active: activeSection === 'profile' }"
      @click="selectSection('profile', '/counter-dashboard')"
    >
      个人档案
    </li>
    <li
      :class="{ active: activeSection === 'dinein' }"
      @click="selectSection('dinein', '/counter-dinein-order')"
    >
      管理堂食订单
    </li>
    <li
      :class="{ active: activeSection === 'tables' }"
      @click="selectSection('tables', '/manage-tables')"
    >
      管理餐桌
    </li>

    <li>
      <span
        @click="toggleSection('delivery')"
        :class="{ active: activeSection === 'delivery' }"
        style="margin-top: 20px; cursor: pointer;"
      >
        <strong>外卖管理</strong>
      </span>
    </li>
    <li
      v-if="activeSection === 'delivery'"
      :class="{ active: activeSubsection === 'assign' }"
      @click="selectSubsection('assign', '/delivery-assign')"
      style="padding-left: 15px;"
    >
      分配外卖员
    </li>
    <li
      v-if="activeSection === 'delivery'"
      :class="{ active: activeSubsection === 'add' }"
      @click="selectSubsection('add', '/delivery-add')"
      style="padding-left: 15px;"
    >
      添加外卖员
    </li>
    <li
      v-if="activeSection === 'delivery'"
      :class="{ active: activeSubsection === 'view' }"
      @click="selectSubsection('view', '/delivery-view')"
      style="padding-left: 15px;"
    >
      查看外卖订单
    </li>

    <li @click="$router.push('/counter-overtime-working')">申请加班</li>
          <li @click="$router.push('/counter-overtime-progress')">我的加班记录</li>
          <li @click="$router.push('/counter-leaving-working')">申请离职</li>
          <li @click="$router.push('/counter-leaving-status')">查看离职申请进度</li>
          <li @click="$router.push('/counter-salary')">工资管理</li>
          <li @click="$router.push('/counter-attendance')">考勤打卡</li>
          <li @click="$router.push('/counter-leave')">请假申请</li>
          <li @click="$router.push('/counter-leave-progress')">我的请假记录</li>
  </ul>
  <div class="logout" @click="logout">退出系统</div>
</div>

  
      <!-- 主体内容 -->
      <div class="form-section">
        <h3>管理堂食订单</h3>
  
        <div v-if="orders.length" class="orders-list">
          <div v-for="order in orders" :key="order.order_id" class="order-item">
            <div class="order-header">
              <div class="order-info">
                <p><strong>订单ID：</strong>{{ order.order_id }}</p>
                <p><strong>用户ID：</strong>{{ order.user_id }}</p>
                <p><strong>桌号：</strong>{{ order.table_number }}</p>
                <p><strong>时间：</strong>{{ formatDate(order.time_ordered) }}</p>
              </div>
              <div class="order-total">
                <p><strong>总价格：</strong>￥{{ order.price.toFixed(2) }}</p>
              </div>
            </div>
  
            <div class="order-details" v-if="!order.is_paid">
              <p><strong>菜品：</strong></p>
              <ul>
                <li v-for="(dish, index) in order.dish_list.split(',')" :key="index">
                  {{ dish.trim() }}
                  <button @click="removeDish(order.order_id, dish.trim())" class="delete-btn">❌ 删除</button>
                </li>
              </ul>
            </div>
            <div class="order-details" v-else>
              <p><strong>菜品：</strong>{{ order.dish_list }}</p>
            </div>
  
            <div class="payment-status">
              <p>
                <strong>支付状态：</strong>
                <span :class="{ unpaid: !order.is_paid }">
                  {{ order.is_paid ? "✅ 已结账" : "❌ 未结账" }}
                </span>
              </p>
              <p v-if="order.is_coupon_used"><strong>使用优惠券：</strong>￥{{ order.discount_amount || 0 }}</p>
              <p v-else><strong>未使用优惠券</strong></p>
            </div>
  
            <div class="action-btn">
              <button v-if="!order.is_paid" @click="openPaymentDialog(order.order_id)">立即结账</button>
              <button v-if="!order.is_paid" @click="cancelOrder(order.order_id)" class="cancel-btn">取消订单</button>
            </div>
          </div>
        </div>
        <div v-else><p>暂无订单记录</p></div>
  
        <!-- 支付弹窗 -->
        <div v-if="isPaymentDialogVisible" class="payment-dialog">
          <div class="dialog-content">
            <h3>选择支付方式</h3>
            <button @click="payWithAlipay">支付宝</button>
            <button @click="payWithWeChat">微信</button>
            <button @click="payWithCard">储值卡</button>
            <button @click="closePaymentDialog" class="dialog-close-btn">关闭</button>
          </div>
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
        activeSection: 'profile',
        activeSubsection: '',

      };
    },
    created() {
      this.fetchOrders();
    },
    methods: {
        selectSection(section, route) {
        this.activeSection = section;
        this.activeSubsection = '';
        this.$router.push(route);
        },

        selectSubsection(subsection, route) {
        this.activeSection = 'delivery';
        this.activeSubsection = subsection;
        this.$router.push(route);
        },

        toggleSection(section) {
        if (this.activeSection === section) {
        this.activeSection = '';
        this.activeSubsection = '';
         } else {
            this.activeSection = section;
            this.activeSubsection = '';
            }
        } ,

      async fetchOrders() {
        try {
          const response = await axios.get("http://localhost:8080/api/order/get-all-orders");
          if (response.data.success) {
            this.orders = response.data.orders;
          } else {
            alert("加载所有订单失败");
          }
        } catch (error) {
          console.error("获取所有订单失败", error);
          alert("网络错误，无法加载所有订单");
        }
      },
      
      formatDate(dateStr) {
        const date = new Date(dateStr);
        return date.toLocaleString();
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
            this.fetchOrders();
          } else {
            alert("支付失败");
          }
        } catch (error) {
          console.error("支付失败", error);
          alert("支付过程中出现错误");
        }
      },
  
      async cancelOrder(orderId) {
        if (!confirm("确定要取消该订单吗？")) return;
        try {
          const res = await axios.delete(`http://localhost:8080/api/order/cancel/${orderId}`);
          if (res.data.success) {
            alert("订单已取消");
            this.fetchOrders();
          } else {
            alert(res.data.message || "取消失败");
          }
        } catch (e) {
          console.error(e);
          alert("取消订单失败");
        }
      },
  
      async removeDish(orderId, dishName) {
        if (!confirm(`确认从订单中删除「${dishName}」？`)) return;
        try {
          const res = await axios.put("http://localhost:8080/api/order/update-dishes", {
            order_id: orderId,
            dish_name: dishName
          });
  
          if (res.data.success) {
            alert(`已删除菜品「${dishName}」，新价格：￥${res.data.new_price.toFixed(2)}`);
            this.fetchOrders();
          } else {
            alert(res.data.message || "删除失败");
          }
        } catch (e) {
          console.error(e);
          alert("删除菜品失败");
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
  .menu-list strong.active {
    color: #00b4d8;
  }
  
  .logout {
    color: #ffb3b3;
    transition: color 0.3s ease;
  }
  
  .logout:hover {
    color: #ffffff;
    font-weight: bold;
  }
  
  /* 主体区域 */
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
  
  /* 订单列表 */
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
    transition: transform 0.3s ease, box-shadow 0.3s ease;
  }
  
  .order-item:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  }
  
  .order-header {
    display: flex;
    justify-content: space-between;
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
  
  .order-details ul {
    padding-left: 18px;
    margin-top: 10px;
  }
  
  .order-details li {
    margin-bottom: 8px;
    font-size: 16px;
  }
  
  .delete-btn {
    margin-left: 10px;
    color: red;
    background: transparent;
    border: none;
    cursor: pointer;
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
  
  .action-btn {
    margin-top: 20px;
    text-align: center;
  }
  
  .action-btn button {
    padding: 12px 28px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 18px;
    cursor: pointer;
    transition: background-color 0.3s;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  }
  
  .action-btn button:hover {
    background-color: #0056b3;
  }
  
  .cancel-btn {
    background-color: #e74c3c;
    margin-left: 10px;
  }
  
  .cancel-btn:hover {
    background-color: #c0392b;
  }
  
  /* 支付弹窗 */
  .payment-dialog {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0,0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
  }
  
  .dialog-content {
    background-color: #fff;
    padding: 30px;
    border-radius: 12px;
    box-shadow: 0 10px 20px rgba(0,0,0,0.1);
    text-align: center;
    width: 90%;
    max-width: 400px;
  }
  
  .dialog-content h3 {
    font-size: 24px;
    margin-bottom: 20px;
    color: #333;
  }
  
  .dialog-content button {
    padding: 12px 25px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 10px;
    font-size: 18px;
    cursor: pointer;
    transition: background-color 0.3s ease;
    width: 100%;
    max-width: 180px;
    margin-bottom: 10px;
  }
  
  .dialog-content button:hover {
    background-color: #0056b3;
  }
  
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
  
  /* 响应式 */
  @media (max-width: 768px) {
    .resume-page {
      flex-direction: column;
    }
    .form-section {
      width: 100vw;
      padding: 30px 20px;
    }
    .sidebar {
      width: 100vw;
      text-align: center;
    }
    .sidebar ul {
      display: flex;
      justify-content: center;
      padding-left: 0;
    }
    .sidebar li {
      display: inline-block;
      padding: 10px 15px;
    }
  }
  </style>
  