Page({
  data: {
    username: '',
    branchId:1,
    items: [],
    totalPrice: 0,
    formattedTotalPrice: '0.00',
    coupons: [],
    remark: '',
    selectedCoupon: 0,
    couponLabels: ['不使用优惠券'],
    selectedCouponLabel: '不使用优惠券',
  },

  onLoad() {
    const saved = wx.getStorageSync('orderData');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        const { username, totalPrice, items } = parsed;
        const price = parseFloat(totalPrice) || 0;
        this.setData({
          username,
          totalPrice: price,
          formattedTotalPrice: price.toFixed(2),
          items,
        });

        if (username && items.length) {
          this.fetchCoupons();
        }
      } catch (e) {
        console.warn('订单数据解析失败', e);
      }
    }
  },

  fetchCoupons() {
    wx.request({
      url: 'http://localhost:8080/api/order/fetch-coupons',
      method: 'POST',
      data: {
        username: this.data.username,
        totalPrice: this.data.totalPrice,
      },
      success: (res) => {
        if (res.data.success) {
          const coupons = res.data.availableCoupons || [];
          const couponLabels = ['不使用优惠券'].concat(
            coupons.map(
              (c) =>
                `优惠券${c.coupon_id}：满${c.min_threshold}减${c.discount_amount}`
            )
          );
          this.setData({ coupons, couponLabels });
        } else {
          wx.showToast({ title: '优惠券加载失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '优惠券请求失败', icon: 'none' });
      },
    });
  },
  onRemarkInput(e) {
    this.setData({
      remark: e.detail.value
    });
  },
  
  onCouponChange(e) {
    const index = e.detail.value;
    const selectedCoupon =
      index === 0 || !this.data.coupons[index - 1]
        ? 0
        : this.data.coupons[index - 1].discount_amount;
    this.setData({
      selectedCoupon,
      selectedCouponLabel: this.data.couponLabels[index],
    });
  },

  confirmOrder() {
    // 确保items中每个元素有name字段，并转成字符串数组
    const stringItems = this.data.items
      .map((item) => {
        if (!item.name) {
          console.warn('item中缺少name:', item);
          return '';
        }
        return item.name.toString();
      })
      .filter((name) => name !== '');

      console.log("提交数据：", {
        username: this.data.username,
        totalPrice: this.data.totalPrice,
        items: stringItems,
        selectedCoupon: this.data.selectedCoupon,
        remark: this.data.remark
      });
      

    wx.request({
      url: 'http://localhost:8080/api/order/submit-order',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
      },
      data: {
        username: this.data.username,
        branchId:wx.getStorageSync('branchId'),
        totalPrice: this.data.totalPrice,
        items: stringItems,
        selectedCoupon: this.data.selectedCoupon,
        remark: this.data.remark  // ✅ 添加备注字段
      },
      
      success: (res) => {
        console.log("后端返回：", res); // 👈 一定要展开看 res.data.success 是 true 还是 false
        if (res.data.success) {
          wx.showToast({
            title: `提交成功 减￥${this.data.selectedCoupon}`,
            icon: 'success',
          });
          setTimeout(() => {
            wx.redirectTo({ url: '/pages/myorder/myorder' });
          }, 3000); // ✅ 延时 3 秒跳转
        } else {
          wx.showToast({
            title: res.data.message || '订单提交失败，请稍后重试',
            icon: 'none'
          });
          console.warn("后端返回失败：", res.data); // 👈 加这个可以看到具体 message
        }
      },
      fail: () => {
        wx.showToast({ title: '网络异常，提交失败', icon: 'none' });
      },
    });
  },
});
