Page({
  data: {
    activeTab: 'home',
    username: wx.getStorageSync('username') || '',
    selectedStore: wx.getStorageSync('selectedStore') || null,
    posters: [
      { img: '/assets/posters/Poster1.png', url: '/pages/promo1/promo1' },
      { img: '/assets/posters/Poster2.png', url: '/pages/promo2/promo2' },
      { img: '/assets/posters/Poster3.png', url: '/pages/promo3/promo3' }
    ]
  },

  onShow() {
    this.setData({
      username: wx.getStorageSync('username') || '',
      selectedStore: wx.getStorageSync('selectedStore') || null
    });
  },

  // 底部 Tab 切换
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (tab === 'home') {
      this.setData({ activeTab: 'home' });
    } else if (tab === 'orders') {
      wx.navigateTo({ url: '/pages/myorder/myorder' });
    } else if (tab === 'profile') {
      wx.navigateTo({ url: '/pages/profile/profile' });
    }
    else if (tab === 'review') {
      wx.navigateTo({ url: '/pages/review/review' });
    }    
  },

  // 开始点餐
  goToMenu() {

    wx.removeStorageSync('branchId');

    wx.navigateTo({ url: '/pages/menu/menu' });
  },

  // 预定餐桌
  goToOrderTable() {

    wx.removeStorageSync('branchId');

    wx.navigateTo({ url: '/pages/ordertable/ordertable' });
  },
  // 外卖点餐
  goToTakeaway() {

    wx.removeStorageSync('branchId');

    wx.navigateTo({ url: '/pages/takeaway/takeaway' });
  },

  // 优惠领券
  goToCoupons() {
    wx.navigateTo({ url: '/pages/coupons/coupons' });
  },

  // 跳转门店选择页
  openStoreSelector() {
    wx.navigateTo({ url: '/pages/store-selector/store-selector' });
  }
});
