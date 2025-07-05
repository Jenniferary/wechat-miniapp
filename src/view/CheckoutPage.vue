<template>
  <div class="container">
    <div class="checkout-form">
      <h2>提交订单</h2>

      <div class="selected-items">
        <h3>已选择的菜品：</h3>
        <ul v-if="items.length">
          <li v-for="item in items" :key="item">{{ item }}</li>
        </ul>
        <p v-else>暂无菜品，请返回菜单选择</p>
      </div>

      <div class="total-price">
        <h3>总价：￥{{ totalPrice.toFixed(2) }}</h3>
      </div>

      <div class="coupon-select">
        <label for="coupon">可用优惠券：</label>
        <select v-model="selectedCoupon" id="coupon">
          <option value="0">不使用优惠券</option>
          <option v-for="c in coupons" :key="c.coupon_id" :value="c.discount_amount">
            优惠券{{ c.coupon_id }}：满{{ c.min_threshold }}减{{ c.discount_amount }}
          </option>
        </select>
      </div>

      <button @click="confirmOrder">确认提交订单</button>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      username: "",
      items: [],
      totalPrice: 0,
      coupons: [],
      selectedCoupon: 0, // 默认不使用优惠券
    };
  },
  created() {
    const saved = localStorage.getItem("orderData");
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        this.username = parsed.username;
        this.totalPrice = parseFloat(parsed.totalPrice); // 确保总价是数值类型
        this.items = parsed.items || []; // 确保 items 是一个数组
        if (this.username && this.items.length) {
          this.fetchCoupons(); // 获取优惠券信息
        }
      } catch (e) {
        console.warn("订单数据解析失败", e);
      }
    }
  },
  methods: {
    // 获取优惠券信息（不提交订单）
    async fetchCoupons() {
      try {
        const res = await axios.post("http://localhost:8080/api/order/fetch-coupons", {
          username: this.username,
          totalPrice: this.totalPrice,
        });
        if (res.data.success) {
          this.coupons = res.data.availableCoupons || [];
        } else {
          alert("优惠券加载失败");
        }
      } catch (err) {
        console.error("优惠券请求失败", err);
      }
    },

    // 确认订单提交
    async confirmOrder() {
      try {
        const response = await axios.post("http://localhost:8080/api/order/submit-order", {
          username: this.username,
          totalPrice: this.totalPrice,
          items: this.items,
          selectedCoupon: this.selectedCoupon,
        });

        if (response.data.success) {
          alert(`订单提交成功！优惠：￥${this.selectedCoupon}`);
          this.$router.push({ path: '/my-account' });
        } else {
          if (response.data.errorCode === "tableNumberNotFound") {
            alert("你还未预定餐桌！");
            this.$router.push({ path: '/ordertable' }); // 跳转到预定餐桌页面
          } else {
            alert("订单提交失败，请稍后再试！");
          }
        }
      } catch (err) {
        console.error("订单提交失败", err);
        alert("提交订单失败！");
      }
    },
  },
};
</script>




<style scoped>
body {
  margin: 0;
  padding: 0;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  background-image: url("./img/background.jpg");
  background-size: cover;
  background-attachment: fixed;
  color: #333;
}

.container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 65vh;
  padding: 20px;
}

.checkout-form {
  background: rgba(255, 255, 255, 0.85);
  padding: 30px 40px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  max-width: 480px;
  width: 100%;
}

.checkout-form h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #2c3e50;
  font-size: 26px;
}

.selected-items,
.total-price,
.coupon-select {
  margin-bottom: 20px;
}

.selected-items ul {
  padding-left: 20px;
}

.selected-items li {
  margin: 6px 0;
}

.total-price h3 {
  font-size: 22px;
  color: #e74c3c;
}

label {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  display: block;
}

select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  background-color: #fff;
  transition: border-color 0.3s;
}

select:focus {
  border-color: #3498db;
  outline: none;
}

button {
  width: 100%;
  padding: 14px;
  font-size: 18px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.2s;
}

button:hover {
  background-color: #2980b9;
  transform: scale(1.05);
}

button:focus {
  outline: none;
}

@media (max-width: 600px) {
  .checkout-form {
    padding: 20px;
  }

  .checkout-form h2 {
    font-size: 22px;
  }

  button {
    font-size: 16px;
  }
}
</style>
