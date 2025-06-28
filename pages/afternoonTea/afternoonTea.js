// pages/afternoonTea/afternoonTea.js
Page({
  data: {
    dessertList: [],
    drinkList: [],
    numAdults: 0,
    numChildren: 0,
    numSeniors: 0,
  },

  onLoad() {
    this.fetchMenuItems();
  },

  fetchMenuItems() {
    wx.request({
      url: 'http://localhost:8080/api/afternoon-tea/desserts',
      success: res => {
        if (Array.isArray(res.data)) {
          const desserts = res.data.map(item => {
            const formattedName = item.itemName.replace(/\s+/g, '');
            return {
              ...item,
              imgUrl: `/images/${formattedName}.jpg`
            };
          });
          this.setData({ dessertList: desserts });
        }
      },
      fail: err => {
        console.error('获取甜品失败:', err);
      }
    });

    wx.request({
      url: 'http://localhost:8080/api/afternoon-tea/drinks',
      success: res => {
        if (Array.isArray(res.data)) {
          const drinks = res.data.map(item => {
            const formattedName = item.itemName.replace(/\s+/g, '');
            return {
              ...item,
              imgUrl: `/images/${formattedName}.jpg`
            };
          });
          this.setData({ drinkList: drinks });
        }
      },
      fail: err => {
        console.error('获取酒水失败:', err);
      }
    });
  },

  onInputAdults(e) {
    const val = parseInt(e.detail.value) || 0;
    this.setData({ numAdults: val });
  },

  onInputChildren(e) {
    const val = parseInt(e.detail.value) || 0;
    this.setData({ numChildren: val });
  },

  onInputSeniors(e) {
    const val = parseInt(e.detail.value) || 0;
    this.setData({ numSeniors: val });
  },

  checkout() {
    const username = wx.getStorageSync('username');
    const totalPrice = this.data.numAdults * 50 + this.data.numChildren * 25 + this.data.numSeniors * 25;

    const itemList = [];
    if (this.data.numAdults > 0) itemList.push({ name: '成人下午茶 * ' + this.data.numAdults, price: 50 * this.data.numAdults });
    if (this.data.numChildren > 0) itemList.push({ name: '儿童下午茶 * '+this.data.numChildren, price: 25*this.data.numChildren });
    if (this.data.numSeniors > 0) itemList.push({ name: '老人下午茶 * '+this.data.numSeniors , price: 25*this.data.numSeniors });

    const orderData = {
      username,
      totalPrice,
      items: itemList,
      cartDetails: {
        numAdults: this.data.numAdults,
        numChildren: this.data.numChildren,
        numSeniors: this.data.numSeniors,
      }
    };

    wx.setStorageSync('orderData', JSON.stringify(orderData));
    wx.navigateTo({ url: '/pages/orderConfirm/orderConfirm' });
  }
});
