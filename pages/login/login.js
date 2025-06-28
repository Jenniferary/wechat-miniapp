Page({
  data: {
    username: '',
    password: '',
    captcha: '',
    captchaUrl: '',
  },

  onLoad() {
    this.getCaptcha(); // 初始加载验证码
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value });
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value });
  },

  onCaptchaInput(e) {
    this.setData({ captcha: e.detail.value });
  },

  getCaptcha() {
    wx.request({
      url: 'http://localhost:8080/api/captcha?time=' + Date.now(),
      method: 'GET',
      responseType: 'arraybuffer',
      success: (res) => {
        const cookie = res.header['Set-Cookie'] || res.header['set-cookie'];
        if (cookie) {
          wx.setStorageSync('cookie', cookie); // 保存 cookie
        }
        const base64 = wx.arrayBufferToBase64(res.data);
        this.setData({
          captchaUrl: 'data:image/jpeg;base64,' + base64
        });
      },
      fail: () => {
        wx.showToast({ title: '获取验证码失败', icon: 'none' });
      }
    });
  },

  refreshCaptcha() {
    this.setData({ captchaUrl: '' });
    setTimeout(() => this.getCaptcha(), 50);
  },

  submitLogin() {
    const { username, password, captcha } = this.data;

    if (!username || !password || !captcha) {
      wx.showToast({ title: '请完整填写所有信息', icon: 'none' });
      return;
    }

    this.userLogin(username, password, captcha)
      .then((res) => {
        if (res.statusCode === 200 && res.data.status === 'success') {
          wx.showToast({ title: '登录成功', icon: 'success' });
          wx.setStorageSync('username', username);
          wx.navigateTo({ url: '/pages/main/main' });
        } else {
          wx.showToast({ title: res.data.message || '登录失败', icon: 'none' });
          this.refreshCaptcha();
        }
      })
      .catch(() => {
        wx.showToast({ title: '网络错误，请稍后重试', icon: 'none' });
      });
  },

  userLogin(username, password, captcha) {
    const cookie = wx.getStorageSync('cookie') || '';
    return new Promise((resolve, reject) => {
      wx.request({
        url: 'http://localhost:8080/api/login',
        method: 'POST',
        header: {
          'content-type': 'application/json',
          'Cookie': cookie
        },
        data: { username, password, captcha },
        success: resolve,
        fail: reject
      });
    });
  },
});
