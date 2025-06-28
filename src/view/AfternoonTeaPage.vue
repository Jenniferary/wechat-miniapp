<template>
  <div class="container">
    <div class="header">
      <h1>食尚阁 - 下午茶自助</h1>
    </div>
    <div class="content">
      <div class="sidebar">
        <h2>选择</h2>
        <ul>
          <li><a href="/my-account">我的/会员</a></li>
          <li><a href="/ordertable">订桌</a></li>
          <li><a href="/package">套餐</a></li>
          <li><a href="/menu">菜单</a></li>
        </ul>
      </div>
      <div class="outer-container">
        <h2>甜品</h2>
        <div class="menu-container">
          <div class="dish" v-for="item in dessertList" :key="item.id">
            <img :src="`/img/${item.itemName}.jpg`" :alt="item.itemName">
            <h3>{{ item.itemName }}</h3>
          </div>
        </div>
      </div>
    </div>

    <div class="content">
      <div class="outer-container">
        <h2>酒水</h2>
        <div class="menu-container">
          <div class="dish" v-for="item in drinkList" :key="item.id">
            <img :src="`/img/${item.itemName}.jpg`" :alt="item.itemName">
            <h3>{{ item.itemName }}</h3>
          </div>
        </div>
      </div>
    </div>

    <div class="checkout-section">
      <h2>选择用餐人数：(50￥一位，儿童，老人半价)</h2>
      <div class="diners-selection">
        <label>成人（12岁以上）：</label>
        <input type="number" v-model.number="numAdults" min="0">
        <label>儿童(12岁以下)：</label>
        <input type="number" v-model.number="numChildren" min="0">
        <label>老人（60岁以上）：</label>
        <input type="number" v-model.number="numSeniors" min="0">
      </div>
      <button class="checkout-btn" @click="checkout">结账</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'AfternoonTea',
  data() {
    return {
      dessertList: [],
      drinkList: [],
      numAdults: 0,
      numChildren: 0,
      numSeniors: 0
    };
  },
  mounted() {
    this.fetchMenuItems();
  },
  methods: {
    fetchMenuItems() {
      axios.get('/api/afternoon-tea/desserts')
        .then(res => this.dessertList = res.data)
        .catch(err => console.error('获取甜品失败:', err));

      axios.get('/api/afternoon-tea/drinks')
        .then(res => this.drinkList = res.data)
        .catch(err => console.error('获取酒水失败:', err));
    },

    checkout() {
      const username = localStorage.getItem("username");
      const totalPrice = this.numAdults * 50.00 + this.numChildren * 25.00 + this.numSeniors * 25.00;

      const orderData = {
      username,  // ✅ 确保 username 被传到 checkout 页
      items: ['afternoon tea'],
      prices: [totalPrice],
      totalPrice,
      cartDetails: {
        numAdults: this.numAdults,
        numChildren: this.numChildren,
        numSeniors: this.numSeniors
  }
};

localStorage.setItem("orderData", JSON.stringify(orderData));
this.$router.push("/checkout");
    }
  }
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
            overflow: hidden;
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
            margin-left: 100px;
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

        .outer-container {
            width: 1200px;
            overflow-x: auto;
            margin-top: 20px;
            padding-bottom: 20px;
        }
        .menu-container {
            display: flex;
            flex-wrap: nowrap;
            justify-content: flex-start;
            width: auto;
            margin: 0 20px;
        }
        .dish {
            margin: 10px;
            text-align: center;
            border: 1px solid #ccc;
            border-radius: 10px;
            padding: 10px;
            background-color: #fff;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            width: 200px;
            box-sizing: border-box;
            flex: 0 0 auto;
        }
        .dish img {
            width: 100%;
            height: 150px;
            border-top-left-radius: 10px;
            border-top-right-radius: 10px;
            object-fit: cover;
        }
        .dish h3 {
            margin-top: 10px;
        }
        .content:nth-child(2) {
            margin-right: 225px;
        }
        .checkout-section {
            text-align: center;
            margin-top: 20px;
        }

        .checkout-btn {
            padding: 12px 24px;
            background-color: #5396c2;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            color: white;
            transition: background-color 0.3s ease;
        }

        .checkout-btn:hover {
            background-color: #4a87b5; /* Darker blue */
        }

        /* Additional styles for the number of diners selection */
        .diners-selection {
            margin-top: 20px;
            text-align: center;
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .diners-selection label {
            font-size: 18px;
            margin-right: 10px;
        }

        .diners-selection input {
            width: 50px;
            padding: 8px;
            font-size: 16px;
            margin-right: 10px;
            text-align: center;
            border: 1px solid #ccc;
            border-radius: 5px;
            box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
            transition: border-color 0.3s ease;
        }

        .diners-selection input:focus {
            outline: none;
            border-color: #5396c2; /* Blue */
        }
    </style>