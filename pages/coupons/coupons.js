Page({
  data: {
    username: wx.getStorageSync('username') || '',
    memberPoints: 0,
    userId: null,
    couponsValid: [],    // 未过期优惠券
    couponsExpired: [],  // 已过期优惠券
    showExpired: false,
    showResult: false,
    lotteryResult: '',
    // 老虎机图标池
    icons: [
      '/assets/icons/coupon1.png',
      '/assets/icons/coupon2.png',
      '/assets/icons/coupon3.png'
    ], 
    reels: [
      '/assets/icons/coupon1.png',
      '/assets/icons/coupon1.png',
      '/assets/icons/coupon1.png'
    ],    
    spinning: false,  // 是否正在转动
  },

  onLoad() {
    this.fetchUserInfo();
  },
  toggleCouponView() {
    this.setData({ showExpired: !this.data.showExpired });
  },  
  fetchUserInfo() {
    const username = this.data.username;
    wx.request({
      url: `http://localhost:8080/api/user/${username}`,
      method: 'GET',
      success: res => {
        if (res.data.status === 'success') {
          const user = res.data.user;
          this.setData({ 
            memberPoints: user.memberPoints,
            userId: user.userId
          });
          this.fetchCoupons(username);
        } else {
          wx.showToast({ title: res.data.message || '用户不存在', icon: 'none' });
        }
      },
      fail: () => wx.showToast({ title: '请求失败', icon: 'none' })
    });
  },

  fetchCoupons(username) {
    wx.request({
      url: `http://localhost:8080/api/coupons/${username}`,
      success: res => {
        if (Array.isArray(res.data)) {
          const now = new Date();
          const couponsValid = [];
          const couponsExpired = [];
          res.data.forEach((c, index) => {
            const expiry = new Date(c.expiryDate);
            const coupon = {
              coupon_id: c.couponId || `id-${index}`,
              min_threshold: c.minThreshold,
              discount_amount: c.discountAmount,
              start_date: this.formatDate(c.startDate),
              expiry_date: this.formatDate(c.expiryDate),
              isExpired: expiry < now
            };
            if (coupon.isExpired) {
              couponsExpired.push(coupon);
            } else {
              couponsValid.push(coupon);
            }
          });
          this.setData({ couponsValid, couponsExpired });
        } else {
          wx.showToast({ title: '获取优惠券失败', icon: 'none' });
        }
      },
      fail: () => wx.showToast({ title: '请求失败', icon: 'none' })
    });
  },  

  formatDate(dateStr) {
    const d = new Date(dateStr);
    return d.toISOString().split('T')[0];
  },

  // 抽奖（老虎机转动）入口
  drawLottery() {
    if (this.data.spinning) return;
    if (this.data.memberPoints < 10) {
      wx.showToast({ title: '积分不足', icon: 'none' });
      return;
    }
  
    this.setData({ spinning: true, lotteryResult: '', showResult: false });
  
    wx.request({
      url: 'http://localhost:8080/api/lottery/draw',
      method: 'POST',
      data: { username: this.data.username },
      success: res => {
        console.log('抽奖接口返回:', res.data);
        if (res.data.success) {
          const icons = this.data.icons;
          let finalReels;
  
          if (res.data.isWin) {
            // 中奖：三个相同
            const idx = Math.floor(Math.random() * icons.length);
            finalReels = [idx, idx, idx];
          } else {
            // 未中奖：三个不全相同
            do {
              finalReels = [
                Math.floor(Math.random() * icons.length),
                Math.floor(Math.random() * icons.length),
                Math.floor(Math.random() * icons.length),
              ];
            } while (finalReels[0] === finalReels[1] && finalReels[1] === finalReels[2]);
          }
  
          let spins = 20;
          const spinInterval = setInterval(() => {
            if (spins > 0) {
              this.setData({
                reels: [
                  icons[Math.floor(Math.random() * icons.length)],
                  icons[Math.floor(Math.random() * icons.length)],
                  icons[Math.floor(Math.random() * icons.length)]
                ]
              });
              spins--;
            } else {
              clearInterval(spinInterval);
              this.setData({
                reels: finalReels.map(i => icons[i]),
                spinning: false,
                showResult: true,
                lotteryResult: res.data.isWin ? '🎉 恭喜中奖！' : '很遗憾，未中奖',
                memberPoints: res.data.memberPoints,
              });
              if (res.data.isWin) {
                this.fetchCoupons(this.data.username);
              }
            }
          }, 100);
        } else {
          wx.showToast({ title: res.data.message || '抽奖失败', icon: 'none' });
          this.setData({ spinning: false });
        }
      },
      fail: () => {
        wx.showToast({ title: '请求失败', icon: 'none' });
        this.setData({ spinning: false });
      }
    });
  },  

  // 中奖后发券给用户，调用后端接口
  grantCoupon() {
    const userId = this.data.userId;
    if (!userId) {
      wx.showToast({ title: '用户信息异常，无法发券', icon: 'none' });
      this.setData({ spinning: false });
      return;
    }

    // 优惠券信息
    const minThreshold = 100;
    const discountAmount = 10;
    const startDate = this.getToday() + 'T00:00';
    const expiryDate = this.getExpiryDate() + 'T23:59';

    wx.request({
      url: 'http://localhost:8080/api/coupons/distribute',
      method: 'POST',
      data: {
        userId: userId,
        startDate: startDate,
        endDate: expiryDate,
        minThreshold: minThreshold,
        discountAmount: discountAmount
      },
      success: res => {
        if (res.data.success) {
          // 刷新优惠券列表
          this.fetchCoupons(this.data.username);
        } else {
          wx.showToast({ title: res.data.message || '发券失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '发券请求失败', icon: 'none' });
      }
    });
  },

  closeResult() {
    this.setData({ showResult: false });
  },

  getToday() {
    const now = new Date();
    return now.toISOString().split('T')[0];
  },

  getExpiryDate() {
    const now = new Date();
    now.setDate(now.getDate() + 10);
    return now.toISOString().split('T')[0];
  }
});
