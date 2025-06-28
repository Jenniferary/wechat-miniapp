<template>
    <div class="container">
      <div class="sidebar">
        <h2>选择</h2>
        <ul>
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
  
      <div class="content">
        <div class="membership-info">
          <h2>会员信息</h2>
          <div v-if="user">
            <p>用户ID：{{ user.userId }}</p>
            <p>用户名：{{ user.username }}</p>
            <p>会员等级：{{ user.memberLevel }}</p>
            <p>账户余额：￥{{ user.memberBalance }}</p>
            <p>会员积分：{{ user.memberPoints }}</p>
          </div>
          <div v-else>
            <p>加载会员信息失败，请稍后再试。</p>
          </div>
        </div>
  
        <div class="membership-actions">
          <h2>会员操作</h2>
          <form @submit.prevent="submitRecharge">
            <label for="amount">充值金额：</label>
            <input type="text" id="amount" v-model="rechargeAmount" placeholder="请输入充值金额" />
            <span class="tooltip-icon" @click="toggleInfoBox">!</span>
            <button type="submit">充值</button>
          </form>
  
          <!-- 提示信息框 -->
          <div v-if="showInfoBox" class="info-box">
            <p class="info-text">1.充值500元成为 VIP1，充值1000元成为 VIP2，充值2000元成为 VIP3。</p>
            <p class="info-text">2.VIP1打九五折，VIP2打八八折，VIP3打七五折</p>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    data() {
      return {
        user: null,
        rechargeAmount: '',
        showInfoBox: false,
        statusMessage: '', // 用于显示充值操作的提示信息
      };
    },
    created() {
      const username = localStorage.getItem("username");
      if (username) {
        this.loadUserInfo(username);
      } else {
        alert("请先登录");
        this.$router.push("/login");
      }
    },
    methods: {
      async loadUserInfo(username) {
        try {
          const response = await axios.get(`http://localhost:8080/api/user/${username}`);
          if (response.data.status === "success") {
            this.user = response.data.user;
          } else {
            alert(response.data.message || "获取用户信息失败");
          }
        } catch (error) {
          console.error("加载用户信息失败", error);
          alert("请求出错：" + error);
        }
      },
  
      async submitRecharge() {
        if (isNaN(this.rechargeAmount) || parseFloat(this.rechargeAmount) <= 0) {
          alert("请输入有效的充值金额！");
          return;
        }
  
        try {
          const response = await axios.post('http://localhost:8080/api/recharge', {
            username: localStorage.getItem("username"),
            amount: parseFloat(this.rechargeAmount),
          });
  
          if (response.data.status === "success") {
            alert("充值成功！");
            this.loadUserInfo(localStorage.getItem("username")); // 重新加载用户信息
          } else {
            this.statusMessage = response.data.message || "充值失败，请稍后再试！";
          }
        } catch (error) {
          console.error('充值操作失败:', error);
          alert("充值失败，请稍后再试！");
        }
      },
  
      toggleInfoBox() {
        this.showInfoBox = !this.showInfoBox;
      },
    },
  };
  </script>
  
  <style scoped>
  /* 页面整体布局 */
  body {
    margin: 0;
    padding: 0;
    font-family: Arial, sans-serif;
    background-image: url("./img/background.jpg");
    background-size: cover;
    color: black;
    background-attachment: fixed;
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
  
  .sidebar h2 {
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
  
  /* 内容区域 */
  .content {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10px;
    max-width: 1100px; /* 内容区域的最大宽度 */
    margin: 0 auto; /* 居中 */
  }
  
  /* 卡片样式 */
  .membership-info,
  .membership-actions {
    background-color: rgba(255, 255, 255, 0.95);
    padding: 30px;
    border-radius: 12px;
    box-shadow: 2px 2px 12px rgba(0, 0, 0, 0.1);
    width: 950px;
    margin-bottom: 25px;
  }
  
  /* 标题样式 */
  .membership-info h2,
  .membership-actions h2 {
    margin-bottom: 15px;
    font-size: 22px;
    color: #2980b9;
    border-bottom: 2px solid #2980b9;
    padding-bottom: 8px;
  }
  
  /* 文字内容 */
  .membership-info p {
    margin-bottom: 10px;
    font-size: 16px;
  }
  
  /* 表单样式 */
  .membership-actions form {
    margin-top: 10px;
  }
  
  .membership-actions label {
    display: block;
    margin-bottom: 8px;
    font-size: 16px;
  }
  
  .membership-actions input[type="text"] {
    width: 100%;
    padding: 10px;
    font-size: 16px;
    margin-top: 5px;
    border: 1px solid #ccc;
    border-radius: 6px;
    box-sizing: border-box;
  }
  
  .membership-actions button {
    margin-top: 15px;
    padding: 10px 20px;
    font-size: 16px;
    background-color: #3498db;
    color: #fff;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.3s;
  }
  
  .membership-actions button:hover {
    background-color: #2980b9;
  }
  
  /* 提示 icon */
  .tooltip-icon {
    display: inline-block;
    margin-left: 10px;
    font-weight: bold;
    cursor: pointer;
    color: #3498db;
  }
  
  /* 提示框 */
  .info-box {
    margin-top: 10px;
    background-color: #ecf0f1;
    padding: 10px;
    border-radius: 6px;
    font-size: 8px;
    color: #2c3e50;
    line-height: 1.5;
  }
  .info-text {
  font-size: 16px; /* 更小的字体 */
    }
  </style>
  