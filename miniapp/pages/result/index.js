const memberApi = require('../../api/member');

function buildResultCopy(ok, type) {
  if (type === 'review') {
    return ok
      ? { title: '反馈已提交', sub: '感谢评价', desc: '感谢你的反馈，欢迎再次光临！' }
      : { title: '提交失败', sub: '反馈未保存', desc: '请返回后重新提交评价。' };
  }

  return ok
    ? { title: '支付完成', sub: '订单已更新', desc: '' }
    : { title: '支付未完成', sub: '订单仍待处理', desc: '如已扣款，请稍后在订单页查看支付状态。' };
}

Page({
  data: {
    ok: true,
    type: 'payment',
    orderId: null,
    resultTitle: '支付完成',
    resultSub: '订单已更新',
    resultDesc: '',
    rewardSummary: null,
    rewardLoading: false
  },

  onLoad(query) {
    const ok = query.ok === '1';
    const type = query.type || 'payment';
    const copy = buildResultCopy(ok, type);
    this.setData({
      ok,
      type,
      orderId: query.orderId ? Number(query.orderId) : null,
      resultTitle: copy.title,
      resultSub: copy.sub,
      resultDesc: copy.desc
    });
  },

  onShow() {
    this.loadRewardSummary();
  },

  async loadRewardSummary() {
    if (!this.data.ok || this.data.type === 'review' || !this.data.orderId) {
      return;
    }
    this.setData({ rewardLoading: true });
    try {
      const rewardSummary = await memberApi.getMemberRewardSummary(this.data.orderId);
      this.setData({ rewardSummary: rewardSummary || null });
    } catch (err) {
      this.setData({ rewardSummary: null });
    } finally {
      this.setData({ rewardLoading: false });
    }
  },

  backHome() {
    wx.reLaunch({ url: '/pages/index/index' });
  },

  goOrder() {
    wx.switchTab({
      url: '/pages/order/index',
      fail: () => {
        wx.reLaunch({ url: '/pages/order/index' });
      }
    });
  },

});
