Page({
  data: {
    username: '',
    password: ''
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  submitRegister(e) {
    const { username, password } = this.data
    if (!username || !password) {
      wx.showToast({ title: '请填写完整', icon: 'none' })
      return
    }

    wx.request({
      url: 'http://localhost:8080/api/register',
      method: 'POST',
      data: { username, password },
      success(res) {
        wx.showToast({ title: '注册成功', icon: 'success' })
        wx.navigateTo({ url: '/pages/login/login' })
      },
      fail(err) {
        wx.showToast({ title: '注册失败', icon: 'none' })
      }
    })
  }
})
