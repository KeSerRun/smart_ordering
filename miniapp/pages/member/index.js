const memberApi = require('../../api/member');
const { isLoggedIn } = require('../../utils/auth');

Page({
  data: {
    loading: false,
    center: null,
    levels: [],
    currentBenefitTags: [],
    benefitOverview: null,
    pointsDeductionSummary: ''
  },

  onShow() {
    if (!isLoggedIn()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      wx.navigateBack({ delta: 1 });
      return;
    }
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const [center, levels] = await Promise.all([
        memberApi.getMemberCenter(),
        memberApi.getMemberLevels()
      ]);
      const benefitOverview = await memberApi.getMemberBenefitOverview().catch(() => null);
      const levelList = Array.isArray(levels) ? levels : [];
      const currentLevel = levelList.find(item => Number(item.id) === Number(center?.levelId || 0)) || null;
      this.setData({
        center: center || null,
        levels: levelList,
        currentBenefitTags: this.parseBenefitConfig(currentLevel ? currentLevel.benefitConfig : ''),
        benefitOverview: benefitOverview || null,
        pointsDeductionSummary: this.buildPointsDeductionSummary(benefitOverview)
      });
    } catch (err) {
      wx.showToast({ title: err.message || '加载会员信息失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  goPoints() {
    wx.navigateTo({ url: '/pages/member-points/index' });
  },

  goGrowth() {
    wx.navigateTo({ url: '/pages/member-growth/index' });
  },

  goCoupon() {
    wx.navigateTo({ url: '/pages/coupon/index' });
  },

  async exchangeCoupon(e) {
    const { id, cost } = e.currentTarget.dataset;
    if (!id) return;
    wx.showLoading({ title: '兑换中', mask: true });
    try {
      await memberApi.exchangeCoupon(id);
      wx.showToast({ title: `已兑换，消耗 ${cost || 0} 积分`, icon: 'none' });
      this.loadData();
    } catch (err) {
      wx.showToast({ title: err.message || '兑换失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  async claimExclusiveCoupon() {
    wx.showLoading({ title: '领取中', mask: true });
    try {
      await memberApi.claimExclusiveCoupon();
      wx.showToast({ title: '专属券已到账', icon: 'none' });
      this.loadData();
    } catch (err) {
      wx.showToast({ title: err.message || '领取失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  buildPointsDeductionSummary(benefitOverview) {
    const rule = benefitOverview && benefitOverview.pointsDeductionRule;
    if (!rule || !rule.enabled) return '';
    const ratio = Number(rule.maxDeductionRatio || 0);
    const ratioText = Number.isFinite(ratio) ? Math.round(ratio * 100) : 0;
    return `每 ${rule.pointsPerStep || 0} 积分抵 ${rule.amountPerStep || 0} 元，单笔最多抵 ${ratioText}%`;
  },

  parseBenefitConfig(raw) {
    if (!raw) return [];
    if (Array.isArray(raw)) {
      return raw.map(item => String(item).trim()).filter(Boolean);
    }
    if (typeof raw === 'string') {
      const text = raw.trim();
      if (!text) return [];
      try {
        const parsed = JSON.parse(text);
        if (Array.isArray(parsed)) {
          return parsed.map(item => String(item).trim()).filter(Boolean);
        }
        if (parsed && typeof parsed === 'object') {
          return Object.entries(parsed)
            .map(([key, value]) => `${key}：${value}`)
            .filter(Boolean);
        }
      } catch (err) {
        return text
          .split(/[,\n，；;|]/)
          .map(item => item.trim())
          .filter(Boolean);
      }
      return [text];
    }
    return [String(raw)];
  }
});
