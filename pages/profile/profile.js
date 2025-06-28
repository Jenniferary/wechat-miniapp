Page({
  data: {
    activeTab: 'profile',
    user: {},
    rechargeAmount: '',
    showInfo: false,
    editPhone: false,      // 手机号编辑状态
    editBirthday: false ,   // 生日编辑状态
    showBirthdayPopup: false
  },

  onLoad() {
    const username = wx.getStorageSync('username') || '';
    if (!username) {
      return wx.redirectTo({ url: '/pages/login/login' });
    }
    this.loadUserInfo(username);
  },

  onShow() {
    this.setData({ activeTab: 'profile' });
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (tab === 'profile') {
      this.setData({ activeTab: 'profile' });
    } else if (tab === 'home') {
      wx.reLaunch({ url: '/pages/main/main' });
    } else if (tab === 'orders') {
      wx.navigateTo({ url: '/pages/myorder/myorder' });
    }
  },

  loadUserInfo(username) {
    wx.request({
      url: `http://localhost:8080/api/user/${username}`,
      method: 'GET',
      success: res => {
        if (res.data.status === 'success') {
          const user = res.data.user;
          const birthday = user.memberBirthday || '';
          const today = new Date();
          const todayStr = `${today.getMonth() + 1}`.padStart(2, '0') + '-' + `${today.getDate()}`.padStart(2, '0');
          const birthdayStr = birthday ? birthday.slice(5) : ''; // 提取 MM-DD
  
          this.setData({
            user: {
              ...user,
              memberBirthday: birthday
            },
            editPhone: !user.memberPhone,
            editBirthday: !user.memberBirthday,
            showBirthdayPopup: birthdayStr === todayStr
          });
        } else {
          wx.showToast({ title: res.data.message || '获取失败', icon: 'none' });
        }
      },
      fail: () => wx.showToast({ title: '网络错误', icon: 'none' })
    });
  },  

  onAmountInput(e) {
    this.setData({ rechargeAmount: e.detail.value });
  },

  toggleInfo() {
    this.setData({ showInfo: !this.data.showInfo });
  },

  submitRecharge() {
    const amount = parseFloat(this.data.rechargeAmount);
    if (isNaN(amount) || amount <= 0) {
      return wx.showToast({ title: '请输入有效金额', icon: 'none' });
    }
    const username = wx.getStorageSync('username');
    wx.request({
      url: 'http://localhost:8080/api/recharge',
      method: 'POST',
      header: { 'content-type': 'application/json' },
      data: { username, amount },
      success: res => {
        if (res.data.status === 'success') {
          wx.showToast({ title: '充值成功', icon: 'success' });
          this.loadUserInfo(username);
          this.setData({ rechargeAmount: '' });
        } else {
          wx.showToast({ title: res.data.message || '充值失败', icon: 'none' });
        }
      },
      fail: () => wx.showToast({ title: '网络错误', icon: 'none' })
    });
  },

  // 编辑控制
  enableEditPhone() {
    this.setData({ editPhone: true });
  },
  enableEditBirthday() {
    this.setData({ editBirthday: true });
  },

  onPhoneInput(e) {
    this.setData({ 'user.memberPhone': e.detail.value });
  },

  onBirthdayInput(e) {
    this.setData({ 'user.memberBirthday': e.detail.value });
  },
  // 跳转至智能推荐页面
  goToLottery() {
    wx.navigateTo({
      url: '/pages/coupons/coupons'
    });
  },
  // 跳转至智能推荐页面
  goToDeepSeek() {
    wx.navigateTo({
      url: '/pages/deepseek/deepseek'
    });
  },

  saveUserInfo() {
    const user = this.data.user;

    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(user.memberPhone)) {
      return wx.showToast({ title: '请输入有效手机号', icon: 'none' });
    }

    const birthdayRegex = /^\d{4}-\d{2}-\d{2}$/;
    if (user.memberBirthday && !birthdayRegex.test(user.memberBirthday)) {
      return wx.showToast({ title: '生日格式应为 YYYY-MM-DD', icon: 'none' });
    }

    wx.request({
      url: 'http://localhost:8080/api/user/update',
      method: 'POST',
      header: { 'content-type': 'application/json' },
      data: {
        username: user.username,
        memberPhone: user.memberPhone,
        memberBirthday: user.memberBirthday
      },
      success: res => {
        if (res.data.status === 'success') {
          wx.showToast({ title: '信息保存成功', icon: 'success' });
          this.setData({
            editPhone: false,
            editBirthday: false
          });
          this.loadUserInfo(user.username);
        } else {
          wx.showToast({ title: res.data.message || '保存失败', icon: 'none' });
        }
      },
      fail: () => wx.showToast({ title: '网络错误', icon: 'none' })
    });
  },
  // 加入这个方法来关闭弹窗
  hideBirthdayPopup() {
    this.setData({ showBirthdayPopup: false });
}
  
});
