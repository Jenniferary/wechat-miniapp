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
        <li><router-link to="/add-manager">
          👤➕ 添加管理员
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
        📦 管理外卖订单
      </h2>
      <div class="action-buttons">
        <router-link
          to="/assign-delivery"
          class="redirect-button"
        >
          🚚 分配外卖员
        </router-link>
        <router-link
          to="/add-delivery-person"
          class="redirect-button"
        >
          ➕ 添加外卖员
        </router-link>
        <router-link
          to="/view-delivery-orders"
          class="redirect-button"
        >
          📄 查看外卖订单
        </router-link>
      </div>
    </div>
  </div>
</template>
  
  <script>
  export default {
    name: "ManageDelivery",
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
    margin-top: 50px;
    text-align: center;
  }
  
  .page-title {
    font-size: 1.8rem;
    color: #2980b9;
    margin-bottom: 2rem;
  }
  
  .action-buttons {
    display: flex;
    justify-content: center;
    gap: 20px;
    flex-wrap: wrap;
  }
  
  .redirect-button {
    padding: 14px 30px;
    background-color: #52a1d7;
    color: white;
    text-align: center;
    border-radius: 8px;
    text-decoration: none;
    font-size: 16px;
    transition: background-color 0.3s ease;
  }
  
  .redirect-button:hover {
    background-color: #3498db;
  }
  </style>