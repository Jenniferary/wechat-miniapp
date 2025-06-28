<template>
    <div class="container">
      <!-- Sidebar -->
      <div class="sidebar">
        <div class="header">
            <h1>食尚阁 - 前台管理系统</h1>
        </div>
        <ul>
          <li><router-link to="/check-orders">（1）管理堂食订单</router-link></li>
          <li><router-link to="/distribute-coupons">（2）管理优惠券</router-link></li>
          <li><router-link to="/manage-delivery">（3）管理外卖订单</router-link></li>
          <li><router-link to="/manage-tables">（4）管理餐桌</router-link></li>
          <li><router-link to="/dishes">（5）管理菜品</router-link></li>
          <li><router-link to="/data-analytics">（6）数据分析</router-link></li>

        </ul>
      </div>
  
      <!-- Content (main page content) -->
      <div class="content">
        <router-view></router-view>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: 'FrontendManagement',
  };
  </script>
  
  <style scoped>
  .container {
    display: flex;
    width:600px;
    justify-content: flex-start;
    min-height: 100vh;
  }
  
  .sidebar {
    background-color: rgba(197, 232, 250, 0.57);
    padding: 20px;
    width: 600px;
    border-radius: 10px;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
    margin-top: 50px;
  }
  
  .header {
    color: #1e1b1b;
    text-align: center;
    padding: 20px;
    box-sizing: border-box;
    margin-bottom: 20px;
    border: none;
    margin-top: 20px;
  }
  
  .sidebar h2 {
    margin-bottom: 20px;
    font-size: 24px;
    color: #2980b9;
    text-align: center;
  }
  
  .sidebar ul {
    list-style-type: none;
    padding: 0;
    margin: 0;
  }
  
  .sidebar ul li {
    margin-bottom: 20px;
  }
  
  .sidebar ul li a {
    text-decoration: none;
    color: #333;
    font-size: 18px;
    display: block;
    text-align: center;
    background-color: rgba(255, 255, 255, 0.26);
    padding: 20px;
    border-radius: 10px;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.3);
    transition: color 0.3s ease;
  }
  
  .sidebar ul li a:hover {
    color: rgba(3, 68, 141, 0.8);
    font-weight: bold;
  }
  </style>
  