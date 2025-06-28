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
  
      <!-- 主体内容 -->
      <div class="order-container">
        <h2>积分兑换</h2>
        <p class="points">当前积分：{{ userPoints.toFixed(2) }}</p>
  
        <div class="orders-list">
          <div class="order-item" v-for="coupon in coupons" :key="coupon.id">
            <div class="order-header">
              <h3>满 {{ coupon.minThreshold }} 减 {{ coupon.discountAmount }}</h3>
              <p>所需积分：{{ coupon.requiredPoints }}</p>
            </div>
  
            <div class="action-btn">
              <button
                :disabled="userPoints < coupon.requiredPoints"
                @click="redeemCoupon(coupon)"
              >
                {{ userPoints >= coupon.requiredPoints ? "立即兑换" : "积分不足" }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import axios from "axios";
  
  export default {
    name: "ExchangePage",
    data() {
      return {
        userPoints: 0,
        coupons: [
          { id: 1, minThreshold: 200, discountAmount: 20, requiredPoints: 1000 },
          { id: 2, minThreshold: 500, discountAmount: 50, requiredPoints: 2500 },
          { id: 3, minThreshold: 1000, discountAmount: 100, requiredPoints: 5000 },
        ],
      };
    },
    created() {
      this.fetchUserPoints();
    },
    methods: {
      async fetchUserPoints() {
        const username = localStorage.getItem("username");
        if (!username) {
          alert("请先登录");
          this.$router.push("/login");
          return;
        }
  
        try {
          const response = await axios.get(
            `http://localhost:8080/api/exchange/points?username=${username}`
          );
          this.userPoints = response.data.points;
        } catch (error) {
          console.error("获取积分失败：", error);
          alert("获取积分失败：" + error.message);
        }
      },
  
      async redeemCoupon(coupon) {
        const username = localStorage.getItem("username");
        try {
          const response = await axios.post(
            "http://localhost:8080/api/exchange/redeem",
            {
              username: username,
              requiredPoints: coupon.requiredPoints,
              minThreshold: coupon.minThreshold,
              discountAmount: coupon.discountAmount,
            }
          );
  
          if (response.data.success) {
            alert("兑换成功！");
            this.fetchUserPoints(); // 更新积分
          } else {
            alert("兑换失败：" + response.data.message);
          }
        } catch (error) {
          console.error("兑换失败：", error);
          alert("兑换出错：" + error.message);
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
    font-size: 20px;
    color: #2980b9;
    border-bottom: 2px solid #2980b9;
    padding-bottom: 10px;
    margin-bottom: 10px;
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
  }
  
  .sidebar ul li a:hover {
    color: #2980b9;
    font-weight: bold;
  }
  
  .order-container {
    width: 900px;
    margin: 0 auto;
    padding: 20px;
    flex: 1;
  }
  
  h2 {
    text-align: center;
    font-size: 32px;
    color: #2c3e50;
    margin-bottom: 20px;
  }
  
  .points {
    text-align: center;
    font-size: 18px;
    color: #444;
    margin-bottom: 30px;
  }
  
  .orders-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  
  .order-item {
    background-color: #ffffff;
    padding: 25px;
    border-radius: 12px;
    box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
  }
  
  .order-header h3 {
    font-size: 20px;
    color: #2c3e50;
    margin-bottom: 10px;
  }
  
  .order-header p {
    font-size: 16px;
    color: #555;
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
  }
  
  button:hover {
    background-color: #2980b9;
  }
  
  button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
  }
  </style>
  