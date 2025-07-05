Page({
  data: {
    latitude: 0,
    longitude: 0,
    storeList: [],
    markers: [],
    loading: true,
    selectedStoreId: null,
  },

  onLoad() {
    wx.getSetting({
      success: res => {
        const isAuthorized = res.authSetting['scope.userLocation'];
        if (isAuthorized) {
          this.getUserLocation();
        } else {
          wx.getLocation({
            type: 'gcj02',
            success: res => {
              this.setData({ latitude: res.latitude, longitude: res.longitude });
              this.loadNearbyStores(res.latitude, res.longitude);
            },
            fail: () => this.handleLocationFail(),
          });
        }
      },
      fail: () => this.handleLocationFail(),
    });
  },

  handleLocationFail() {
    wx.showModal({
      title: '需要授权定位',
      content: '为获取附近餐厅，请允许获取当前位置',
      confirmText: '去设置',
      success: modalRes => {
        if (modalRes.confirm) {
          wx.openSetting({
            success: settingRes => {
              if (settingRes.authSetting['scope.userLocation']) {
                this.getUserLocation();
              } else {
                wx.showToast({ title: '未授权定位', icon: 'none' });
                this.setData({ loading: false });
              }
            }
          });
        } else {
          this.setData({ loading: false });
        }
      }
    });
  },

  getUserLocation() {
    wx.getLocation({
      type: 'gcj02',
      success: res => {
        this.setData({ latitude: res.latitude, longitude: res.longitude });
        this.loadNearbyStores(res.latitude, res.longitude);
      },
      fail: () => {
        wx.showToast({ title: '定位失败', icon: 'none' });
        this.setData({ loading: false });
      }
    });
  },

  loadNearbyStores(lat, lng) {
    wx.request({
      url: 'http://localhost:8080/api/restaurant/nearby',
      method: 'GET',
      data: { lat, lng },
      success: res => {
        if (res.data.status === 'success') {

          console.log(res);

          const stores = res.data.data.sort((a, b) => a.distanceKm - b.distanceKm);

          stores.forEach(store => {
            store.distanceStr = store.distanceKm.toFixed(2) + ' km';
          });

          const markers = [
            {
              id: 0,
              latitude: lat,
              longitude: lng,
              width: 30,
              height: 30,
              callout: {
                content: '当前位置',
                color: '#fff',
                fontSize: 14,
                bgColor: '#007aff',
                padding: 6,
                borderRadius: 6,
                display: 'ALWAYS'
              }
            }
          ];

          stores.forEach(store => {
            markers.push({
              id: store.id,
              latitude: store.latitude,
              longitude: store.longitude,
              width: 40,
              height: 40,
              callout: {
                content: `${store.name}\n电话: ${store.phone}\n距离: ${store.distanceStr}`,
                color: '#333',
                fontSize: 14,
                bgColor: '#fff',
                padding: 8,
                borderRadius: 8,
                display: 'BYCLICK'
              }
            });
          });

          this.setData({
            storeList: stores,
            markers,
            selectedStoreId: stores.length > 0 ? stores[0].id : null,
            latitude: stores.length > 0 ? stores[0].latitude : lat,
            longitude: stores.length > 0 ? stores[0].longitude : lng,
            loading: false
          });
        } else {
          wx.showToast({ title: '加载失败', icon: 'none' });
          this.setData({ loading: false });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络错误', icon: 'none' });
        this.setData({ loading: false });
      }
    });
  },

  markertap(e) {
    const id = e.markerId;
    if (id === 0) return; // 点击当前位置不处理
    const store = this.data.storeList.find(s => s.id === id);
    if (!store) return;

    this.setData({
      selectedStoreId: id,
      latitude: store.latitude,
      longitude: store.longitude,
    });
  },

  selectStore(e) {
    const store = e.currentTarget.dataset.store;
    this.setData({
      selectedStoreId: store.id,
      latitude: store.latitude,
      longitude: store.longitude,
    });
  },

  goToMenu(e) {
    const store = e.currentTarget.dataset.store;
    if (!store || !store.id) {
      wx.showToast({ title: '请选择餐厅', icon: 'none' });
      return;
    }

    wx.setStorageSync('branchId', store.id)
    wx.navigateTo({
      url: `/pages/ordertable/ordertable`
    });
  },

  goToTakeaway(e) {
    const store = e.currentTarget.dataset.store;
    if (!store || !store.id) {
      wx.showToast({ title: '请选择餐厅', icon: 'none' });
      return;
    }
    wx.setStorageSync('branchId', store.id)
    wx.navigateTo({
      url: `/pages/takeaway/takeaway`

    });
  }
});
