Page({
  data: {
    dishList: [],
    deliveryAddress: "",
    deliveryDate: "",
    deliveryTime: "",
    selectedCouponId: 0,
<<<<<<< HEAD
    branchId:1,
=======
>>>>>>> origin/structured
    selectedCouponText: "选择优惠券",
    coupons: [],
    couponDescriptions: [],
    username: "",
    totalPrice: 0,
    today: "",
    remark:""
  },
  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
  },
  
  onLoad() {
    const now = new Date();
    const todayStr = now.toISOString().split("T")[0];
    this.setData({ today: todayStr });

    const orderData = wx.getStorageSync('deliveryOrderData') || {};
    const username = orderData.username || wx.getStorageSync('username');

    if (!username) {
      wx.showToast({ title: "未登录，无法下单", icon: "none" });
      wx.redirectTo({ url: "/pages/login/login" });
      return;
    }

    this.setData({
      username,
      dishList: orderData.dishList || [],
      totalPrice: orderData.totalPrice || 0
    });

    wx.request({
      url: `http://localhost:8080/api/user/${username}`,
      method: 'GET',
      success: (res) => {
        if (res.data.status === "success") {
          this.fetchCoupons();
        } else {
          wx.showToast({ title: "加载用户信息失败", icon: "none" });
        }
      },
      fail: () => {
        wx.showToast({ title: "请求失败", icon: "none" });
      }
    });
  },

  fetchCoupons() {
    wx.request({
      url: 'http://localhost:8080/api/takeaway/fetch-coupons',
      method: 'POST',
      data: {
        username: this.data.username,
        totalPrice: this.data.totalPrice
      },
      success: (res) => {
        if (res.data.success && Array.isArray(res.data.availableCoupons)) {
          const coupons = res.data.availableCoupons.map(c => ({
            couponId: c.coupon_id,
            minThreshold: c.min_threshold,
            discountAmount: c.discount_amount
          }));
          const descriptions = ["选择优惠券"].concat(
            coupons.map(c => `优惠券${c.couponId}：满${c.minThreshold}减${c.discountAmount}`)
          );
          this.setData({ coupons, couponDescriptions: descriptions });
        } else {
          wx.showToast({ title: "暂无优惠券", icon: "none" });
        }
      },
      fail: () => {
        wx.showToast({ title: "加载优惠券失败", icon: "none" });
      }
    });
  },

  onAddressChange(e) {
    this.setData({ deliveryAddress: e.detail.value });
  },

  onDateChange(e) {
    this.setData({ deliveryDate: e.detail.value });
  },

  onTimeChange(e) {
    this.setData({ deliveryTime: e.detail.value });
  },

  onCouponChange(e) {
    const index = parseInt(e.detail.value);
    if (index === 0) {
      this.setData({ selectedCouponId: 0, selectedCouponText: "选择优惠券" });
    } else {
      const selected = this.data.coupons[index - 1];
      this.setData({
        selectedCouponId: selected.couponId,
        selectedCouponText: `优惠券${selected.couponId}：满${selected.minThreshold}减${selected.discountAmount}`
      });
    }
  },

  submitOrder() {
    const { deliveryAddress, deliveryDate, deliveryTime, selectedCouponId, dishList, username, totalPrice } = this.data;

    if (!deliveryAddress || !deliveryDate || !deliveryTime) {
      wx.showToast({ title: "请填写完整送达信息", icon: "none" });
      return;
    }

    const estimatedTime = `${deliveryDate} ${deliveryTime}`;

    wx.request({
      url: 'http://localhost:8080/api/takeaway/submit',
      method: 'POST',
      data: {
        username,
<<<<<<< HEAD
        branchId:wx.getStorageSync('branchId'),
=======
>>>>>>> origin/structured
        address: deliveryAddress,
        estimatedTime,
        items: dishList.map(d => d.name),
        totalPrice,
        selectedCoupon: selectedCouponId || null,
        remark: this.data.remark
      },
      success: (res) => {
        if (res.data.success) {
          wx.removeStorageSync("deliveryOrderData");
          wx.showToast({ title: "下单成功", icon: "success" });
          setTimeout(() => {
            wx.redirectTo({ url: "/pages/myorder/myorder" }); // ✅ 关键跳转
          }, 1600); // 避免跳转太快 toast 没显示完
        } else {
          wx.showToast({ title: "下单失败: " + (res.data.message || "未知错误"), icon: "none" });
        }
      },
      fail: () => {
        wx.showToast({ title: "请求失败", icon: "none" });
      }
    });
  }
});
