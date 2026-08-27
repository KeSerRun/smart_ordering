const couponApi = require('../../api/coupon');

Page({
  data: {
    activeStatus: '',
    tabs: [
      { label: '全部', value: '' },
      { label: '未使用', value: 0 },
      { label: '已使用', value: 1 },
      { label: '已过期', value: 2 }
    ],
    loading: false,
    coupons: [],
    availableCount: 0,
    usedCount: 0,
    expiredCount: 0
  },

  onShow() {
    this.loadCoupons();
  },

  async loadCoupons() {
    this.setData({ loading: true });
    try {
      const params = { pageNum: 1, pageSize: 100 };
      if (this.data.activeStatus !== '') {
        params.status = this.data.activeStatus;
      }
      const result = await couponApi.getMyCoupons(params);
      const coupons = result.list || [];
      const availableCount = coupons.filter(item => Number(item.status) === 0).length;
      const usedCount = coupons.filter(item => Number(item.status) === 1).length;
      const expiredCount = coupons.filter(item => Number(item.status) === 2).length;
      this.setData({ coupons, availableCount, usedCount, expiredCount });
    } catch (err) {
      wx.showToast({ title: err.message || '加载优惠券失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  switchTab(e) {
    const { value } = e.currentTarget.dataset;
    this.setData({ activeStatus: value }, () => {
      this.loadCoupons();
    });
  }
});
