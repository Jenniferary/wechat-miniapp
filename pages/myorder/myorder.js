Page({
  data: {
    activeTab: 'orders',
    dineInOrders: [],
    takeawayOrders: [],
    currentOrderType: 'dinein',
    showPayPopup: false,
    selectedOrderId: null
  },

  onLoad() {
    const username = wx.getStorageSync('username');
    if (!username) {
      return wx.redirectTo({ url: '/pages/login/login' });
    }

    // 堂食订单
    wx.request({
      url: `http://localhost:8080/api/order/my-orders?username=${username}`,
      method: 'GET',
      success: (res) => {
        if (res.data.success && Array.isArray(res.data.orders)) {
          const dineInOrders = res.data.orders.map(o => {
            return {
              order_id: o.order_id,
              table_number: o.table_number,
              dish_list: o.dish_list,
              price: o.price,
              time_ordered: o.time_ordered,
              timeFormatted: this.formatTime(o.time_ordered),
              priceFixed: Number(o.price).toFixed(2),
              paid: o.is_paid,
              remark: o.remark || '' 
            };
          });
          this.setData({ dineInOrders });
        }
      }
    });

    // 外卖订单
    wx.request({
      url: `http://localhost:8080/api/takeaway/orders/${username}`,
      method: 'GET',
      success: (res) => {
        if (Array.isArray(res.data)) {
          const takeawayOrders = res.data.map(o => ({
            order_id: Number(o.orderId),
            dish_list: o.dishList,
            delivery_address: o.deliveryAddress,
            price: o.price,
            discount: o.discountAmount,
            coupon: o.isCouponUsed,
            estimated: this.formatTime(o.estimatedDeliveryTime),
            deliveryTime: this.formatTime(o.deliveryTime),
            status: o.deliveryStatus,
            deliveryPersonId: o.deliveryPersonId,
            remark: o.remark || '' 
          }));
          this.setData({ takeawayOrders });
        }
      }
    });
  },

  formatTime(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    if (isNaN(date)) return '';
    const Y = date.getFullYear();
    const M = (date.getMonth() + 1).toString().padStart(2, '0');
    const D = date.getDate().toString().padStart(2, '0');
    const h = date.getHours().toString().padStart(2, '0');
    const m = date.getMinutes().toString().padStart(2, '0');
    return `${Y}-${M}-${D} ${h}:${m}`;
  },

  switchOrderType(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({ currentOrderType: type });
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (tab === 'orders') {
      this.setData({ activeTab: 'orders' });
    } else if (tab === 'home') {
      wx.reLaunch({ url: '/pages/main/main' });
    }  else if (tab === 'review') {
      wx.navigateTo({ url: '/pages/review/review' });
    }else if (tab === 'profile') {
      wx.navigateTo({ url: '/pages/profile/profile' });
    }
  },
  confirmDelivery(e) {
    const orderId = e.currentTarget.dataset.orderId;
    console.log('confirmDelivery orderId:', orderId, typeof orderId);
  
    wx.request({
      url: 'http://localhost:8080/api/takeaway/confirm',
      method: 'POST',
      header: { 'content-type': 'application/json' },
      data: { orderId },
      success: (res) => {
        if (res.data.success) {
          wx.showToast({ title: '确认收货成功', icon: 'success' });
          this.refreshTakeawayOrders();
        } else {
          wx.showToast({ title: res.data.message || '确认失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },  

  refreshTakeawayOrders() {
    const username = wx.getStorageSync('username');
    if (!username) return;
    wx.request({
      url: `http://localhost:8080/api/takeaway/orders/${username}`,
      method: 'GET',
      success: (res) => {
        if (Array.isArray(res.data)) {
          const takeawayOrders = res.data.map(o => ({
            order_id: Number(o.orderId),
            dish_list: o.dishList,
            delivery_address: o.deliveryAddress,
            price: o.price,
            discount: o.discountAmount,
            coupon: o.isCouponUsed,
            estimated: this.formatTime(o.estimatedDeliveryTime),
            deliveryTime: this.formatTime(o.deliveryTime),
            status: o.deliveryStatus,
            deliveryPersonId: o.deliveryPersonId,
            remark: o.remark || '' 
          }));
          this.setData({ takeawayOrders });
        }
      }
    });
  },

  showPayOptions(e) {
    const orderId = e.currentTarget.dataset.orderId;
    this.setData({
      selectedOrderId: orderId,
      showPayPopup: true
    });
  },

  hidePayOptions() {
    this.setData({
      showPayPopup: false,
      selectedOrderId: null
    });
  },

  pay(e) {
    const method = e.currentTarget.dataset.method;
    const orderId = this.data.selectedOrderId;
  
    wx.request({
      url: 'http://localhost:8080/api/order/checkout',
      method: 'POST',
      data: {
        order_id: orderId,
        payment_method: method  // 改这里
      },
      success: (res) => {
        if (res.data.success) {
          wx.showToast({ title: '支付成功', icon: 'success' });
          this.updateOrderStatus(orderId);
        } else {
          wx.showToast({ title: res.data.message || '支付失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '请求失败', icon: 'none' });
      },
      complete: () => {
        this.hidePayOptions();
      }
    });
  },
  

  updateOrderStatus(orderId) {
    const dineInOrders = this.data.dineInOrders.map(o => {
      if (o.order_id === orderId) {
        return Object.assign({}, o, { paid: true });
      }
      return o;
    });
    this.setData({ dineInOrders });
  }
});
