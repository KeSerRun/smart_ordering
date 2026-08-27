Component({
  properties: {
    title: {
      type: String,
      value: ''
    },
    backgroundColor: {
      type: String,
      value: '#fffdf9'
    },
    textColor: {
      type: String,
      value: '#251819'
    },
    showBack: {
      type: Boolean,
      value: true
    }
  },

  data: {
    statusBarHeight: 20,
    navBarHeight: 44,
    totalHeight: 64
  },

  lifetimes: {
    attached() {
      const windowInfo = typeof wx.getWindowInfo === 'function'
        ? wx.getWindowInfo()
        : wx.getSystemInfoSync();
      const menuButton = wx.getMenuButtonBoundingClientRect();
      const statusBarHeight = Number(windowInfo.statusBarHeight) || 20;
      const navBarHeight = menuButton && menuButton.top
        ? (menuButton.top - statusBarHeight) * 2 + menuButton.height
        : 44;

      this.setData({
        statusBarHeight,
        navBarHeight,
        totalHeight: statusBarHeight + navBarHeight
      });
    }
  },

  methods: {
    handleBack() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        wx.navigateBack();
        return;
      }

      wx.switchTab({ url: '/pages/index/index' });
    }
  }
});
