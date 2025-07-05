<template> 
  <div class="container">
    <div class="sidebar">
      <h2>导航</h2>
      <ul>
          <li><router-link to="/menu">菜单</router-link></li>
          <li><router-link to="/my-account">用户主页</router-link></li>
          <li><router-link to="/ordertable">订桌</router-link></li>
          <li><router-link to="/package">套餐</router-link></li>
          <li><router-link to="/orders">历史订单</router-link></li>
          <li><router-link to="/takeaway-order">外送订单</router-link></li>
          <li><router-link to="/reviews">菜品评价</router-link></li>
          <li><router-link to="/exchange">积分兑换</router-link></li>
      </ul>
    </div>

    <div class="content">
      <h2>我的优惠券</h2>
      <div class="coupon-group">
        <div v-if="coupons.length > 0">
          <div
            v-for="coupon in sortedCoupons"
            :key="coupon.couponId"
            :class="['coupon-item', { 'expired-coupon': !isActive(coupon.expiryDate) }]"
          >
            <div class="coupon-header">
              <p class="coupon-id">优惠券ID: {{ coupon.couponId }}</p>
              <p class="coupon-status" :class="{'active': isActive(coupon.expiryDate), 'expired': !isActive(coupon.expiryDate)}">
                {{ isActive(coupon.expiryDate) ? '有效' : '过期' }}
              </p>
            </div>
            <div class="coupon-details">
              <p><strong>开始日期:</strong> {{ formatDate(coupon.startDate) }}</p>
              <p><strong>过期日期:</strong> {{ formatDate(coupon.expiryDate) }}</p>
              <p><strong>最低消费金额:</strong> ￥{{ coupon.minThreshold }}</p>
              <p><strong>折扣金额:</strong> ￥{{ coupon.discountAmount }}</p>
            </div>
          </div>
        </div>
        <div v-else>
          <p>您没有可用的优惠券。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      coupons: [],
    };
  },
  mounted() {
    this.fetchCoupons();
  },
  computed: {
    // 按过期状态排序：有效在上，过期在下
    sortedCoupons() {
      return this.coupons.slice().sort((a, b) => {
        return this.isActive(b.expiryDate) - this.isActive(a.expiryDate);
      });
    },
  },
  methods: {
    fetchCoupons() {
      const username = localStorage.getItem("username");
      if (username) {
        fetch(`/api/coupons/${username}`)
          .then(response => response.json())
          .then(data => {
            this.coupons = data || [];
          })
          .catch(error => {
            console.error("Error fetching coupons:", error);
          });
      }
    },
    formatDate(dateString) {
      const date = new Date(dateString);
      const options = {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false,
      };
      return date.toLocaleString('zh-CN', options);
    },
    isActive(expiryDate) {
      const currentDate = new Date();
      return new Date(expiryDate) > currentDate;
    },
  },
};
</script>

<style scoped>
body {
  margin: 0;
  padding: 0;
  font-family: 'Arial', sans-serif;
  background-image: url("./img/background.jpg");
  background-size: cover;
  color: #333;
  background-attachment: fixed;
}

.container {
  display: flex;
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.sidebar {
  background-color: rgba(255, 255, 255, 0.9);
  padding: 20px;
  width: 250px;
  border-radius: 15px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.sidebar h2 {
  margin-bottom: 20px;
  font-size: 22px;
  color: #2980b9;
  font-weight: bold;
  text-transform: uppercase;
  border-bottom: 2px solid #2980b9;
  padding-bottom: 10px;
}

.sidebar ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.sidebar ul li {
  margin-bottom: 15px;
}

.sidebar ul li a {
  text-decoration: none;
  color: #333;
  font-size: 18px;
  transition: color 0.3s ease;
}

.sidebar ul li a:hover {
  color: #2980b9;
  font-weight: bold;
}

.content {
  flex: 1;
  padding: 30px;
  background-color: #f9f9f9;
  border-radius: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

h2 {
  font-size: 28px;
  color: #2980b9;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30px;
}

.coupon-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.coupon-item {
  background-color: #fff;
  padding: 20px;
  border-radius: 10px;
  width: 780px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

.coupon-item:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  transform: translateY(-5px);
}

.expired-coupon {
  background-color: #e0e0e0 !important;
  color: #888 !important;
}

.expired-coupon .coupon-status {
  color: #c0392b !important;
}

.coupon-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.coupon-id {
  font-size: 16px;
  font-weight: bold;
}

.coupon-status {
  font-size: 14px;
  font-weight: bold;
  color: green;
}

.coupon-status.active {
  color: #2ecc71;
}

.coupon-details p {
  margin: 10px 0;
  font-size: 16px;
}

.coupon-details strong {
  color: #2980b9;
}
</style>
