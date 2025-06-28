// pages/menu/menu.js
Page({
  data: {
    dishes: [],
    cart: [],
    totalPrice: 0,
  },

  onLoad() {
    wx.request({
      url: 'http://localhost:8080/api/menu',
      success: (res) => {
        if (Array.isArray(res.data)) {
          const updatedDishes = res.data.map(dish => {
            const formattedName = dish.dishName.replace(/\s+/g, '');
            dish.imgUrl = `/images/${formattedName}.jpg`;
            dish.dishStock = dish.dishStock|| 0;
            return dish;
          });
          this.setData({ dishes: updatedDishes });
        }
      }
    });
  },

  addToCart(e) {
    const { name, price } = e.currentTarget.dataset;
    const cart = this.data.cart.concat({ name, price: parseFloat(price) });
    const total = (this.data.totalPrice + parseFloat(price)).toFixed(2);
    this.setData({
      cart,
      totalPrice: parseFloat(total)
    });
    wx.showToast({ title: `${name}已加入购物车`, icon: 'success' });
  },

  removeFromCart(e) {
    const { name, price } = e.currentTarget.dataset;
    const index = this.data.cart.findIndex(item => item.name === name);
    if (index !== -1) {
      const cart = [...this.data.cart];
      cart.splice(index, 1);
      const total = (this.data.totalPrice - parseFloat(price)).toFixed(2);
      this.setData({
        cart,
        totalPrice: parseFloat(total)
      });
      wx.showToast({ title: `${name}已移除`, icon: 'none' });
    }
  },

  onImageError(e) {
    const index = e.currentTarget.dataset.index;
    const dishes = this.data.dishes;
    dishes[index].imgUrl = '/images/default.jpg';
    this.setData({ dishes });
  },

  checkout() {
    const cart = this.data.cart.map(item => ({
      name: item.name,
      price: parseFloat(item.price)
    }));

    const orderData = {
      username: wx.getStorageSync('username'),
      totalPrice: this.data.totalPrice,
      items: cart
    };
    wx.setStorageSync('orderData', JSON.stringify(orderData));
    wx.navigateTo({ url: '/pages/orderConfirm/orderConfirm' });
  }
});