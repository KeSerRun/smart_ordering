const memberApi = require('../../api/member');

Page({
  data: {
    records: [],
    loading: false,
    loadingMore: false,
    finished: false,
    pageNum: 1,
    pageSize: 20,
    total: 0
  },

  onShow() {
    this.resetAndLoad();
  },

  onPullDownRefresh() {
    this.resetAndLoad(true);
  },

  onReachBottom() {
    this.loadData(false);
  },

  resetAndLoad(withRefresh = false) {
    this.setData({
      records: [],
      pageNum: 1,
      total: 0,
      finished: false
    }, () => {
      this.loadData(true, withRefresh);
    });
  },

  async loadData(reset = false, withRefresh = false) {
    if ((this.data.loading && reset) || this.data.loadingMore || this.data.finished) {
      if (withRefresh) wx.stopPullDownRefresh();
      return;
    }

    const nextPage = reset ? 1 : this.data.pageNum;
    this.setData(reset ? { loading: true } : { loadingMore: true });
    try {
      const result = await memberApi.getMemberPointsRecords({
        pageNum: nextPage,
        pageSize: this.data.pageSize
      });
      const newList = Array.isArray(result.list) ? result.list : [];
      const records = reset ? newList : this.data.records.concat(newList);
      const total = Number(result.total || 0);
      const finished = records.length >= total || newList.length < this.data.pageSize;

      this.setData({
        records,
        total,
        pageNum: nextPage + 1,
        finished
      });
    } catch (err) {
      wx.showToast({ title: err.message || '加载积分明细失败', icon: 'none' });
    } finally {
      this.setData({ loading: false, loadingMore: false });
      if (withRefresh) wx.stopPullDownRefresh();
    }
  }
});
