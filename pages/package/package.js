Page({
  data: {
    packages: [],
    cart: [],
    totalPrice: 0,
    formattedTotalPrice: '0.00',
  },

  onLoad() {
    wx.request({
      url: 'http://localhost:8080/api/packages',
      success: (res) => {
        if (Array.isArray(res.data)) {
          const updatedPackages = res.data.map(pkg => {
            pkg.dishList = pkg.dishList.map(dish => {
              const formattedName = dish.dishName.replace(/\s+/g, '');
              return {
                ...dish,
                imgUrl: `/images/${formattedName}.jpg`
              };
            });
            return pkg;
          });
          this.setData({ packages: updatedPackages });
        }
      }
    });
  },

  onImageError(e) {
    const index = e.currentTarget.dataset.index;
    const packages = this.data.packages;

    packages.forEach(pkg => {
      if (pkg.dishList && pkg.dishList[index]) {
        pkg.dishList[index].imgUrl = '/img/default.jpg';
      }
    });

    this.setData({ packages });
  },

  increasePackage(e) {
    const packageData = e.currentTarget.dataset.package;

    const newItem = {
      ...packageData,
      quantity: 1, // 固定显示为 1，避免 confirm 页面需要数量
    };

    const cart = [...this.data.cart, newItem];
    this.setData({ cart }, this.updateTotalPrice);
  },

  removeFromCart(e) {
    const index = e.currentTarget.dataset.index;
    wx.showModal({
      title: '提示',
      content: '确认删除该套餐？',
      success: (res) => {
        if (res.confirm) {
          const cart = [...this.data.cart];
          cart.splice(index, 1);
          this.setData({ cart }, this.updateTotalPrice);
        }
      },
    });
  },

  updateTotalPrice() {
    const total = this.data.cart.reduce((sum, item) => {
      return sum + (item.packagePrice * (item.quantity || 1));
    }, 0);
    this.setData({
      totalPrice: total,
      formattedTotalPrice: total.toFixed(2),
    });
  },
  decreasePackage(e) {
    const packageData = e.currentTarget.dataset.package;
    const cart = [...this.data.cart];
    const indexToRemove = cart.findIndex(item => item.packageName === packageData.packageName);
  
    if (indexToRemove !== -1) {
      cart.splice(indexToRemove, 1);
      this.setData({ cart }, this.updateTotalPrice);
    }
  },
  checkout() {
    const dishNames = this.data.cart.flatMap(item =>
      Array.isArray(item.dishList) ? item.dishList.map(d => d.dishName) : []
    );

    const totalPrice = this.data.totalPrice;

    const orderData = {
      username: wx.getStorageSync('username'),
      items: dishNames,
      totalPrice,
      formattedTotalPrice: totalPrice.toFixed(2),
      selectedCoupon: 0,
      remark: '',
    };

    wx.setStorageSync('orderData', JSON.stringify(orderData));
    wx.navigateTo({ url: '/pages/packageConfirm/packageConfirm' });
  }
});
