const bannerApi = require('../../api/banner');
const { isLoggedIn, wxLogin, phoneLogin } = require('../../utils/auth');
const { KEYS, get, remove } = require('../../utils/storage');

Page({
  data: {
    statusBarHeight: 0,
    navBarHeight: 44,
    loggedIn: false,
    userInfo: null,
    agreeProtocol: false,
    banners: []
  },

  onLoad() {
    this.initNavBar();
  },

  onShow() {
    this.loadBanners();
    const loggedIn = isLoggedIn();
    const userInfo = get(KEYS.USER_INFO) || null;
    this.setData({ loggedIn, userInfo });
  },

  async loadBanners() {
    try {
      const banners = await bannerApi.getBannerList('PROFILE_HERO');
      this.setData({ banners: Array.isArray(banners) ? banners : [] });
    } catch (err) {
      if (!Array.isArray(this.data.banners) || this.data.banners.length === 0) {
        this.setData({ banners: [] });
      }
    }
  },

  initNavBar() {
    const { statusBarHeight } = wx.getWindowInfo();
    const menuBtn = wx.getMenuButtonBoundingClientRect();
    this.setData({
      statusBarHeight,
      navBarHeight: (menuBtn.top - statusBarHeight) * 2 + menuBtn.height
    });
  },

  /* ========== 登录 ========== */
  async handlePhoneLogin(e) {
    if (!this.data.agreeProtocol) {
      wx.showToast({ title: '请先勾选用户协议与隐私政策', icon: 'none' });
      return;
    }
    if (!e.detail.code) {
      wx.showToast({ title: e.detail.errMsg || '请授权手机号', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '登录中', mask: true });
    try {
      const code = await wxLogin();
      await phoneLogin(code, e.detail.code);
      const userInfo = get(KEYS.USER_INFO) || null;
      this.setData({ loggedIn: true, userInfo });
      wx.showToast({ title: '登录成功', icon: 'none' });
    } catch (err) {
      wx.showToast({ title: err.message || '登录失败', icon: 'none', duration: 2000 });
    } finally {
      wx.hideLoading();
    }
  },

  handlePhoneLoginTap() {
    if (this.data.agreeProtocol) {
      return;
    }
    wx.showToast({ title: '请先勾选用户协议与隐私政策', icon: 'none' });
  },

  toggleAgreeProtocol() {
    this.setData({ agreeProtocol: !this.data.agreeProtocol });
  },

  openUserAgreement() {
    wx.showModal({
      title: '用户协议',
      content: '登录前请阅读并同意《用户协议》。当前先使用说明弹窗占位，后续可接正式协议页。',
      showCancel: false
    });
  },

  openPrivacyPolicy() {
    wx.showModal({
      title: '隐私政策',
      content: '登录前请阅读并同意《隐私政策》。当前先使用说明弹窗占位，后续可接正式隐私政策页。',
      showCancel: false
    });
  },

  handleLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确定退出当前账号吗？退出后仍可继续浏览和点餐。',
      success: (res) => {
        if (!res.confirm) return;
        remove(KEYS.TOKEN);
        remove(KEYS.OPENID);
        remove(KEYS.USER_INFO);
        this.setData({ loggedIn: false, userInfo: null });
        wx.showToast({ title: '已退出登录', icon: 'none' });
      }
    });
  },

  /* ========== 导航 ========== */
  goMyReview() {
    wx.navigateTo({ url: '/pages/my-review/index' });
  },

  goOrder() {
    wx.switchTab({ url: '/pages/order/index' });
  },

  goCoupon() {
    wx.navigateTo({ url: '/pages/coupon/index' });
  },

  goMember() {
    wx.navigateTo({ url: '/pages/member/index' });
  },

  handleQuickEntryAction() {
    this.goFeedback();
  },

  goFeedback() {
    wx.navigateTo({ url: '/pages/feedback/index' });
  },

  goAbout() {
    wx.showModal({
      title: '关于云点餐',
      content: '云点餐 — 智能堂食点单系统\n版本 1.0.0\n\n致力于为餐厅提供便捷的扫码点餐体验',
      showCancel: false
    });
  },

  openBanner(e) {
    const banner = e.currentTarget.dataset.banner || {};
    const targetPath = banner.targetPath || '';
    const actionType = Number(banner.actionType || 0);

    if (!targetPath || actionType === 0) return;

    if (actionType === 2) {
      wx.switchTab({ url: targetPath });
      return;
    }

    wx.navigateTo({ url: targetPath });
  },

  clearCache() {
    wx.showModal({
      title: '清除缓存',
      content: '确定要清除本地缓存吗？这会清除桌台信息和本地临时记录。',
      success: (res) => {
        if (res.confirm) {
          remove(KEYS.TABLE);
          remove(KEYS.ORDER_ID);
          remove(KEYS.ORDERED_DISH_IDS);
          remove(KEYS.MOCK_PAID_ORDER_IDS);
          remove(KEYS.REVIEWED_ORDER_IDS);
          remove(KEYS.PERSON_COUNT);
          wx.showToast({ title: '本地缓存已清除', icon: 'none' });
        }
      }
    });
  }
});
