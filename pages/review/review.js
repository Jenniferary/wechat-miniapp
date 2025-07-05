Page({
  data: {
    activeTab: 'review',
    orders: [],
    ratingMap: {},   // { orderId: rating }
    commentMap: {}   // { orderId: comment }
  },

  onLoad() {
    this.setData({ activeTab: 'review' });
    const username = wx.getStorageSync('username');
    if (!username) {
      wx.redirectTo({ url: '/pages/login/login' });
      return;
    }

    wx.request({
      url: `http://localhost:8080/api/orders?username=${username}`,
      method: 'GET',
      success: res => {
        if (Array.isArray(res.data)) {
          this.setData({ orders: res.data });
        }
      }
    });
  },

  onRatingChange(e) {
    const orderId = e.currentTarget.dataset.orderId;
    const val = parseInt(e.detail.value) + 1;
    const updated = this.data.orders.map(o => {
      if (o.order_id === orderId) o.tempRating = val;
      return o;
    });
    this.setData({ orders: updated });
  },

  onCommentInput(e) {
    const orderId = e.currentTarget.dataset.orderId;
    const comment = e.detail.value;
    const updated = this.data.orders.map(o => {
      if (o.order_id === orderId) o.tempComment = comment;
      return o;
    });
    this.setData({ orders: updated });
  },
  selectStar(e) {
    const score = e.currentTarget.dataset.score;
    const orderId = e.currentTarget.dataset.orderId;
    const orders = this.data.orders.map(o => {
      if (o.order_id === orderId) o.tempRating = score;
      return o;
    });
    this.setData({ orders });
  },  
  submitReview(e) {
    const orderId = e.currentTarget.dataset.orderId;
    const order = this.data.orders.find(o => o.order_id === orderId);
    const rating = order.tempRating;
    const comment = order.tempComment;

    if (!rating || !comment) {
      wx.showToast({ title: '请填写完整评价', icon: 'none' });
      return;
    }

    wx.request({
      url: 'http://localhost:8080/api/submit-review',
      method: 'POST',
      data: {
        order_id: orderId,
        rating,
        comment
      },
      success: res => {
        wx.showToast({ title: '评价成功', icon: 'success' });
        this.onLoad(); // 重新加载数据刷新状态
      }
    });
  },
  // 底部 Tab 切换
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (tab === 'review') {
      this.setData({ activeTab: 'review' });
    } else if (tab === 'orders') {
      wx.navigateTo({ url: '/pages/myorder/myorder' });
    } else if (tab === 'profile') {
      wx.navigateTo({ url: '/pages/profile/profile' });
    }
    else if (tab === 'home') {
      wx.reLaunch({ url: '/pages/main/main' });
    }    
  }
});
