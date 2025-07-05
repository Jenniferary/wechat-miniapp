<template>
    <div class="container">
      <div class="header">
        <h1>食尚阁 - 点菜系统</h1>
      </div>
      <div class="content">
        <div class="sidebar">
          <h2>选择</h2>
          <ul>
            <li><router-link to="/my-account">我的/会员</router-link></li>
            <li><router-link to="/ordertable">订桌</router-link></li>
            <li><router-link to="/my_ordertable">已预定的餐桌</router-link></li>
            <li><router-link to="/package">套餐</router-link></li>
            <li><router-link to="/buffet">自助餐</router-link></li>
          </ul>
        </div>
        <div class="menu-container">
          <div v-for="dish in dishes" :key="dish.id" class="dish">
            <img :src="`/img/${encodeURIComponent(dish.dishName)}.jpg`" :alt="dish.dishName" />

            <h3>{{ dish.dishName }}</h3>
            <p>价格: ￥{{ dish.dishPrice }}</p>
            <span class="add-to-cart" @click="addToCart(dish.dishName, dish.dishPrice)">+</span>
            <span class="remove-from-cart" @click="removeFromCart(dish.dishName, dish.dishPrice)">-</span>
          </div>
        </div>
      </div>
      <div class="cart">
        <h2>购物车</h2>
        <div id="shopping-cart">
          <div v-for="item in cart" :key="item.name">
            <p>{{ item.name }} - ￥{{ item.price }}</p>
          </div>
          <div>Total Price: ￥{{ totalPrice }}</div>
        </div>
        <button @click="checkout">提交堂食订单</button>
        <button @click="checkoutDelivery" class="checkout-button">提交外卖订单</button>
      </div>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    data() {
      return {
        dishes: [],
        cart: [],
        totalPrice: 0
      };
    },
    created() {
      this.loadMenu();
    },
    methods: {
      async loadMenu() {
        try {
          const response = await axios.get('http://localhost:8080/api/menu');
          this.dishes = response.data;
        } catch (error) {
          console.error('Error loading menu:', error);
        }
      },
      addToCart(dishName, dishPrice) {
        this.cart.push({ name: dishName, price: dishPrice });
        this.totalPrice += dishPrice;
        this.renderCart();
        alert(dishName + "已加入购物车！");
      },
      removeFromCart(dishName, dishPrice) {
        const index = this.cart.findIndex(item => item.name === dishName);
        if (index !== -1) {
          this.cart.splice(index, 1);
          this.totalPrice -= dishPrice;
          this.renderCart();
          alert(dishName + "已从购物车移除！");
        }
      },
      renderCart() {
        // 购物车已在页面中通过 Vue 渲染，若需要其他操作可在此扩展
      },
      async checkout() { 
      const orderData = {
      username: localStorage.getItem("username"),
      totalPrice: this.totalPrice.toFixed(2),
      items: this.cart.map(item => item.name), // 只获取菜品名称
        };

        // 将订单数据存储到 localStorage
        localStorage.setItem("orderData", JSON.stringify(orderData));

        // 跳转到提交订单页面
       this.$router.push("/checkout"); 
    },

      
      checkoutDelivery() {
      const deliveryOrderData = {
      username: localStorage.getItem("username"),
      totalPrice: this.totalPrice.toFixed(2),
      dishList: this.cart.map(item => ({
        name: item.name,
        price: item.price
       }))
      };
     localStorage.setItem("deliveryOrderData", JSON.stringify(deliveryOrderData));
     // 跳转到外卖订单填写页
  this.$router.push("/delivery-checkout");
},

      submitOrder(action) {
        const form = this.createForm(action);
        this.cart.forEach(item => {
          this.addItemToForm(form, item.name, item.price);
        });
        this.addTotalPriceToForm(form);
        this.submitForm(form);
      },
      createForm(action) {
        const form = document.createElement('form');
        form.setAttribute('method', 'post');
        form.setAttribute('action', action);
        return form;
      },
      addItemToForm(form, itemName, itemPrice) {
        const itemNameInput = this.createHiddenInput('item[]', itemName);
        const itemPriceInput = this.createHiddenInput('price[]', itemPrice);
        form.appendChild(itemNameInput);
        form.appendChild(itemPriceInput);
      },
      addTotalPriceToForm(form) {
        const totalPriceInput = this.createHiddenInput('totalPrice', this.totalPrice);
        form.appendChild(totalPriceInput);
      },
      createHiddenInput(name, value) {
        const input = document.createElement('input');
        input.setAttribute('type', 'hidden');
        input.setAttribute('name', name);
        input.setAttribute('value', value);
        return input;
      },
      submitForm(form) {
        document.body.appendChild(form);
        form.submit();
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
  
  .menu-container {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    max-width: 800px;
    width: 100%;
    margin-top: 20px;
  }
  
  .dish {
    margin: 10px;
    text-align: center;
    border: 1px solid #ccc;
    border-radius: 10px;
    padding: 10px;
    background-color: #fff;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
    width: calc(45% - 20px);
    box-sizing: border-box;
  }
  
  .dish img {
    width: 100%;
    height: auto;
    border-radius: 5px;
  }
  
  .dish p {
  font-size: 14px;
  color: #555;
  margin: 8px 0;
}

  
  .add-to-cart,
  .remove-from-cart {
    cursor: pointer;
    font-size: 24px;
    color: blue;
    margin-right: 10px;
    outline: none;
  }
  
  .add-to-cart:active,
  .remove-from-cart:active {
    transform: scale(0.9);
  }
  
  .cart {
    margin-top: 30px;
    border-top: 2px solid #000;
    padding-top: 20px;
    width: 100%;
    max-width: 800px;
    box-sizing: border-box;
  }
  
  .cart h2 {
    text-align: center;
    margin-bottom: 20px;
  }
  
  #shopping-cart {
    text-align: center;
  }
  
  #shopping-cart p {
  font-size: 14px;
  margin: 4px 0;
  }

  #shopping-cart div {
  font-size: 14px;
  }

  button {
    display: block;
    margin: 0 auto;
    padding: 10px 20px;
    font-size: 18px;
    background-color: #3498db;
    color: #fff;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    transition: background-color 0.3s ease;
  }
  
  button:hover {
    background-color: #2980b9;
  }
  
  .checkout-button {
    margin-top: 10px;
    background-color: #52a1d7;
  }
  
  .checkout-button:hover {
    background-color: #64a9d7;
  }
  </style>
  