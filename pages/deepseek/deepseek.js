Page({
  data: {
    input: '',
    reply: ''
  },

  onInput(e) {
    this.setData({ input: e.detail.value });
  },

  sendQuery() {
    const input = this.data.input.trim();
    if (!input) {
      return wx.showToast({ title: '请输入内容', icon: 'none' });
    }

    wx.request({
      url: 'http://localhost:8080/api/chat', // 修改为你部署后的服务器地址
      method: 'POST',
      header: { 'content-type': 'application/json' },
      data: { input },
      success: res => {
        if (res.data.reply) {
          this.setData({ reply: res.data.reply });
        } else {
          wx.showToast({ title: '大模型未返回结果', icon: 'none' });
        }
      },
      fail: () => wx.showToast({ title: '请求失败', icon: 'none' })
    });
  }
});
