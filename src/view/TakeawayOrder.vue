<template>
  <div class="container">
    <div class="checkout-form">
      <h2>提交外卖订单</h2>

      <div class="selected-items">
        <h3>已选择的菜品：</h3>
        <ul>
          <li v-for="dish in dishList" :key="dish.name">
            {{ dish.name }}：￥{{ dish.price }}
          </li>
        </ul>
      </div>
      

      <div class="form-group">
        <label>送达地址：</label>
        <input v-model="deliveryAddress" type="text" required />
      </div>

      <div class="form-group">
        <label>预期送达时间：</label>
        <input v-model="deliveryTime" type="datetime-local" required />
      </div>

      <div class="coupon-select">
        <select v-model="selectedCouponId">
          <option value="0">选择优惠券</option>
          <option v-for="coupon in coupons" :key="coupon.couponId" :value="coupon.couponId">
            优惠券{{ coupon.couponId }}：满{{ coupon.minThreshold }}减{{ coupon.discountAmount }}
          </option>
        </select>
      </div>

      <button @click="submitOrder">确认提交订单</button>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      dishList: [],
      deliveryAddress: "",
      deliveryTime: "",
      selectedCouponId: 0,
      coupons: [],
      userId: null,
      username: "",
      totalPrice: 0,
    };
  },
  computed: {
    calculatedPrice() {
      return this.dishList.reduce((sum, item) => sum + item.price, 0).toFixed(2);
    },
  },
  async created() {
    const orderData = JSON.parse(localStorage.getItem("deliveryOrderData") || "{}");

    this.username = orderData.username;
    this.dishList = orderData.dishList || [];
    this.totalPrice = parseFloat(orderData.totalPrice || 0);

    if (!this.username) {
      alert("未登录，无法下单");
      this.$router.push("/login");
      return;
    }

    try {
      const res = await axios.get(`http://localhost:8080/api/user/${this.username}`);
      if (res.data.status === "success") {
          this.userId = res.data.user.userId;
          await this.fetchCoupons();
      }
    } catch (err) {
      alert("加载用户信息失败");
    }
  },
  methods: {
    async fetchCoupons() {
  try {
    const res = await axios.post("http://localhost:8080/api/takeaway/fetch-coupons", {
      username: this.username,
      totalPrice: this.totalPrice,
    });

    console.log("原始后端数据：", res.data);

    if (res.data.success && Array.isArray(res.data.availableCoupons)) {
      this.coupons = res.data.availableCoupons.map(coupon => ({
        couponId: coupon.coupon_id,
        minThreshold: coupon.min_threshold,
        discountAmount: coupon.discount_amount
      }));

      console.log("转换后的前端优惠券数据：", this.coupons);
    } else {
      console.warn("未获取到优惠券数组！");
    }
  } catch (err) {
    console.error("加载优惠券失败", err);
  }
},

    async submitOrder() {
      if (!this.deliveryAddress || !this.deliveryTime) {
        alert("请填写送达地址和时间");
        return;
      }

      try {
        const selectedCoupon = this.selectedCouponId !== 0 ? this.selectedCouponId : null;

        const res = await axios.post("http://localhost:8080/api/takeaway/submit", {
          username: this.username,
          address: this.deliveryAddress,
          estimatedTime: this.deliveryTime,
          items: this.dishList.map(dish => dish.name), // 发送菜品名称列表
          totalPrice: this.totalPrice,
          selectedCoupon: selectedCoupon,
        });

        if (res.data.success) {
          alert("下单成功！");
          localStorage.removeItem("deliveryOrderData");
          this.$router.push("/takeaway-order");
        } else {
          alert("下单失败：" + (res.data.message || "未知错误"));
        }
      } catch (err) {
        alert("下单失败：" + (err.response?.data?.message || err.message));
      }
    },
  },
};
</script>

<style scoped>
.container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}
.checkout-form {
  width: 500px;
  background: rgba(255, 255, 255, 0.9);
  padding: 30px;
  border-radius: 10px;
}
.form-group {
  margin-bottom: 20px;
}
button {
  padding: 12px 25px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
button:hover {
  background-color: #2980b9;
}
</style>
