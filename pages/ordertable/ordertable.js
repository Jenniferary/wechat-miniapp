Page({
  data: {
    form: {
      tableType: 'normal',
      numOfPeople: '',
      username: wx.getStorageSync('username') || ''
    },
    minDate: '',
    reservationDate: '',
    reservationTime: '12:00',
    successMessage: '',
    errorMessage: ''
  },

  onLoad() {
    const now = new Date();
    // 格式化最小可选日期 YYYY-MM-DD
    const y = now.getFullYear();
    const m = (now.getMonth() + 1).toString().padStart(2, '0');
    const d = now.getDate().toString().padStart(2, '0');
    const today = `${y}-${m}-${d}`;
    this.setData({
      minDate: today,
      reservationDate: today
    });
  },

  onTypeChange(e) {
    this.setData({
      'form.tableType': e.detail.value
    });
  },

  onNumChange(e) {
    this.setData({
      'form.numOfPeople': e.detail.value
    });
  },

  onDateChange(e) {
    this.setData({
      reservationDate: e.detail.value
    });
  },

  onTimeChange(e) {
    this.setData({
      reservationTime: e.detail.value
    });
  },

  submitReservation() {
    this.setData({ successMessage: '', errorMessage: '' });
    const { tableType, numOfPeople, username } = this.data.form;
    const { reservationDate, reservationTime } = this.data;
    wx.request({
      url: 'http://localhost:8080/api/reserve',
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: {
        tableType,
        numOfPeople,
        username,
        reservationTime: `${reservationDate} ${reservationTime}`
      },
      success: (res) => {
        if (res.data.status === 'success') {
          this.setData({
            successMessage: `预定成功！您的桌号为：${res.data.tableNumber}`
          });
        } else {
          this.setData({ errorMessage: res.data.message });
        }
      },
      fail: () => {
        this.setData({ errorMessage: '请求失败，请稍后重试' });
      }
    });
  },

  goMenu() {
    wx.navigateTo({ url: '/pages/menu/menu' });
  }
});
