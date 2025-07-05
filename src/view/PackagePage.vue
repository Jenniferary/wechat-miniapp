<template>
  <div class="container">
    <div class="header">
      <h1>食尚阁 - 套餐</h1>
    </div>
    <div class="content">
      <div class="sidebar">
        <h2>选择</h2>
        <ul>
          <li><a href="/my-account">我的/会员</a></li>
          <li><a href="/ordertable">订桌</a></li>
          <li><a href="/menu">菜单</a></li>
          <li><a href="/buffet">自助餐</a></li>
        </ul>
      </div>
      <div class="menu-container">
        <div v-for="pkg in packages" :key="pkg.packageId" class="scrollable-menu-container">
          <div class="dish">
            <h3>
              {{ pkg.packageName }}
              <span class="add-to-cart" @click="addToCart(pkg)">+</span>
            </h3>
            <p>价格: {{ pkg.packagePrice }}</p>
            <ul>
              <li v-for="dish in pkg.dishList" :key="dish.id">
                {{ dish.dishName }} - ￥{{ dish.dishPrice }}
              </li>
            </ul>
            <img v-for="dish in pkg.dishList" :key="dish.id" :src="'/img/' + dish.dishName + '.jpg'" :alt="dish.dishName" />
          </div>
        </div>
      </div>
    </div>

    <div class="shopping-cart">
      <h2>购物车</h2>
      <ul>
        <li v-for="(item, index) in cart" :key="index">
          {{ item.packageName }} - ￥{{ item.packagePrice }}
          <span class="remove-from-cart" @click="removeFromCart(index)">-</span>
        </li>
      </ul>
      <button class="checkout-btn" @click="checkout">结账</button>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "PackagePage",
  data() {
    return {
      packages: [],
      cart: [],
    };
  },
  mounted() {
    this.fetchPackages();
  },
  methods: {
    fetchPackages() {
      axios.get("/api/packages").then((res) => {
        this.packages = res.data;
      });
    },
    addToCart(pkg) {
      this.cart.push(pkg);
      alert("已成功将套餐添加到购物车！");
    },
    removeFromCart(index) {
      if (confirm("确认要删除吗？")) {
        this.cart.splice(index, 1);
      }
    },
    checkout() {
  const username = localStorage.getItem("username"); // 从 localStorage 获取用户名
  const items = this.cart.map((item) => item.packageName);
  const prices = this.cart.map((item) => item.packagePrice);
  const total = prices.reduce((acc, curr) => acc + curr, 0);

  const orderData = {
    username: username, // ✅ 加上这行
    items: items,
    prices: prices,
    totalPrice: total,
    cartDetails: this.cart,
  };
  localStorage.setItem("orderData", JSON.stringify(orderData));
  this.$router.push("/checkout");
},
  },
};
</script>
  
  <style scoped>
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
            flex-direction: column;
            align-items: center;
            min-height: 100vh;
        }

        .header {
            color: #1e1b1b;
            text-align: center;
            padding: 20px;
            width: 100%;
            box-sizing: border-box;
            margin-bottom: 20px;
            border: none;
            margin-top: 20px;
        }

        .content {
            display: flex;
            justify-content: center;
            width: 100%;
            box-sizing: border-box;
            padding: 20px;
        }

        .sidebar {
            background-color: rgba(255, 255, 255, 0.8);
            padding: 20px;
            width: 200px;
            border-radius: 10px;
            box-sizing: border-box;
            margin-right: 20px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            height: calc(100vh - 40px);
            overflow-y: auto;
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

        .menu-container {
            display: flex;
            flex-wrap: wrap;
            justify-content: space-between;
            max-width: 800px;
            width: 100%;
            margin-top: 20px;
        }

        .dish {
            margin-bottom: 20px;
            text-align: center;
            border: 1px solid #ccc;
            border-radius: 10px;
            padding: 10px;
            background-color: #fff;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            width: calc(100% - 10px);
            box-sizing: border-box;
        }

        .dish img {
            width: 100%;
            height: auto;
            border-radius: 5px;
        }

        .scrollable-menu-container {
            max-height: 400px;
            overflow-y: auto;
            width: 50%;
            margin-bottom: 20px;
        }
        .add-to-cart, .remove-from-cart {
            color: #2980b9; /* 蓝色 */
            font-size: 20px;
            cursor: pointer;
            margin-left: 5px;
        }
        .shopping-cart {
            margin-top: 20px;
            padding: 20px;
            background-color: rgba(255, 255, 255, 0.8);
            border-radius: 10px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 800px; /* 控制购物车的最大宽度 */
            margin-bottom: 20px;
            box-sizing: border-box;
        }

        .shopping-cart h2 {
            color: #2980b9;
            margin-bottom: 10px;
            text-align: center; /* 文字居中 */
        }

        .shopping-cart ul {
            list-style-type: none;
            padding: 0;
            text-align: center; /* 列表项文字居中 */
        }

        .shopping-cart li {
            margin-bottom: 10px;
        }

        .checkout-btn {
            display: block;
            width: 100%;
            padding: 10px;
            background-color: #2980b9;
            color: #fff;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            transition: background-color 0.3s ease;
            box-sizing: border-box;
            max-width: 200px; /* 控制按钮的最大宽度 */
            margin: 0 auto; /* 居中显示 */
        }

        .checkout-btn:hover {
            background-color: #1c638e;
        }
    </style>
