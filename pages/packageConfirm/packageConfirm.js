Page({
  data: {
    username: '',
    dishList: [],
    quantity: 1,
    totalPrice: 0,
    formattedTotalPrice: '0.00',
    coupons: [],
    couponLabels: ['不使用优惠券'],
    selectedCoupon: 0,
    selectedCouponLabel: '不使用优惠券',
    remark: '',
  },

  onLoad() {
    const orderDataStr = wx.getStorageSync('orderData');
    if (orderDataStr) {
      try {
        const data = JSON.parse(orderDataStr);
        this.setData({
          username: data.username,
          dishList: data.items.map(name => ({ dishName: name })),
          totalPrice: data.totalPrice,
          formattedTotalPrice: data.formattedTotalPrice,
          selectedCoupon: data.selectedCoupon,
          remark: data.remark,
        });
        if (data.username) this.fetchCoupons();
      } catch (e) {
        console.error('订单数据解析失败:', e);
      }
    } else {
      console.warn('没有获取到 orderData');
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
            coupons.map(c => `优惠券${c.coupon_id}：满${c.min_threshold}减${c.discount_amount}`)
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
    this.setData({ remark: e.detail.value });
  },

  onCouponChange(e) {
    const index = e.detail.value;
    const selectedCoupon = index === 0 ? 0 : this.data.coupons[index - 1].discount_amount;
    this.setData({
      selectedCoupon,
      selectedCouponLabel: this.data.couponLabels[index],
    });
  },

  confirmOrder() {
    const items = this.data.dishList.map(d => d.dishName);

    wx.request({
      url: 'http://localhost:8080/api/order/submit-order',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
      },
      data: {
        username: this.data.username,
        items,
        totalPrice: this.data.totalPrice,
        selectedCoupon: this.data.selectedCoupon,
        remark: this.data.remark,
      },
      success: (res) => {
        if (res.data.success) {
          wx.showToast({
            title: `提交成功 减￥${this.data.selectedCoupon}`,
            icon: 'success',
          });
          setTimeout(() => {
            wx.redirectTo({ url: '/pages/myorder/myorder' });
          }, 3000);
        } else {
          wx.showToast({ title: res.data.message || '订单提交失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络异常，提交失败', icon: 'none' });
      },
    });
  },
});
