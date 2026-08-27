const bannerApi = require('../../api/banner');
const couponApi = require('../../api/coupon');
const dishApi = require('../../api/dish');
const { KEYS, get } = require('../../utils/storage');
const { isLoggedIn, wxLogin, phoneLogin } = require('../../utils/auth');
const { bindTableByCode, ensureCurrentUserTableBinding, normalizeTableCode } = require('../../utils/table-binding');

function formatCouponRule(coupon) {
  if (!coupon) return '';
  if (Number(coupon.couponType) === 1) {
    return `满${coupon.thresholdAmount}减${coupon.discountAmount}`;
  }
  return `${coupon.discountRate}折优惠`;
}

function formatCouponValue(coupon) {
  if (!coupon) return '';
  if (Number(coupon.couponType) === 1) {
    return `¥${coupon.discountAmount}`;
  }
  return `${coupon.discountRate}折`;
}

Page({
  data: {
    statusBarHeight: 0,
    navBarHeight: 44,
    loggedIn: false,
    tableCode: '',
    table: null,
    showLoginPanel: false,
    agreeProtocol: false,
    loading: false,
    banners: [],
    coupons: [],
    categories: [],
    heroTitle: '今天吃点招牌热菜',
    heroDesc: '首页先看活动、领券，再去点餐。',
    heroTag: '堂食点餐',
    sceneTag: '今日主推',
    tableStatusText: '未绑定桌台',
    sceneEntry: false,
    autoRedirected: false,
    sceneRefreshPending: false
  },

  onLoad(options) {
    this.initNavBar();
    this.loadCategories();
    const code = this.extractTableCode(options);
    if (code) {
      this.setData({ tableCode: code, loading: true, sceneEntry: true, sceneRefreshPending: true });
    }
    this.refreshHeroState();
  },

  onShow() {
    // 首页每次展示时刷新轮播，避免私有桶预签名 URL 过期后图片消失。
    this.loadBanners();
    const loggedIn = isLoggedIn();
    const table = get(KEYS.TABLE);
    const sceneTableCode = normalizeTableCode(this.data.tableCode);
    const cachedTableCode = normalizeTableCode(table && table.code);
    this.setData({ loggedIn, table: table || null, showLoginPanel: false }, () => {
      this.refreshHeroState();
    });

    if (sceneTableCode && this.data.sceneRefreshPending) {
      this.setData({ table: null, loading: true, autoRedirected: false }, () => {
        this.refreshHeroState();
      });
      this.autoBind(this.data.tableCode);
      return;
    }

    if (sceneTableCode && !table) {
      this.autoBind(this.data.tableCode);
      return;
    }

    if (table) {
      this.setData({ tableCode: table.code || '' });
      this.tryRedirectToMenu();
    }

    if (loggedIn) {
      this.loadCouponPreview();
    } else {
      this.setData({ coupons: [] });
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

  async loadBanners() {
    try {
      const banners = await bannerApi.getBannerList('HOME');
      this.setData({ banners: Array.isArray(banners) ? banners : [] });
    } catch (err) {
      // 刷新失败时保留当前轮播，避免网络抖动或 URL 过期导致首页整块消失。
      if (!Array.isArray(this.data.banners) || this.data.banners.length === 0) {
        this.setData({ banners: [] });
      }
    }
  },

  async loadCategories() {
    try {
      const categories = await dishApi.getCategoryList();
      this.setData({
        categories: Array.isArray(categories) ? categories.slice(0, 8) : []
      });
    } catch (err) {
      this.setData({ categories: [] });
    }
  },

  async loadCouponPreview() {
    try {
      const result = await couponApi.getMyCoupons({ status: 0, pageNum: 1, pageSize: 3 });
      const coupons = Array.isArray(result.list) ? result.list : [];
      this.setData({
        coupons: coupons.map(item => ({
          ...item,
          displayRule: formatCouponRule(item),
          displayValue: formatCouponValue(item)
        }))
      });
    } catch (err) {
      this.setData({ coupons: [] });
    }
  },

  refreshHeroState() {
    const { table, loading } = this.data;
    let heroTitle = '今天吃点招牌热菜';
    let heroDesc = '首页先看活动、领券，再去点餐。';
    let sceneTag = '今日主推';
    let tableStatusText = '未绑定桌台';

    if (loading) {
      heroTitle = '正在识别桌台';
      heroDesc = '稍候片刻，马上进入点餐状态。';
      sceneTag = '识别中';
      tableStatusText = '读取桌台信息';
    } else if (table) {
      heroTitle = '热菜上新，直接开点';
      heroDesc = '桌台已就绪，优惠券和活动都可以直接用。';
      sceneTag = '桌台已就绪';
      tableStatusText = `${table.code || '-'} ${table.name || ''}`.trim();
    }

    this.setData({
      heroTitle,
      heroDesc,
      sceneTag,
      tableStatusText
    });
  },

  /**
   * 从 URL 参数中提取桌号编码
   * @param {Object} options - 页面 onLoad 的 options
   * @returns {string}
   */
  extractTableCode(options) {
    if (!options) return '';
    if (options.scene) {
      const decoded = decodeURIComponent(options.scene);
      if (decoded.includes('code=')) return decoded.split('code=')[1].split('&')[0];
      return decoded;
    }
    if (options.code) return String(options.code);
    if (options.q) {
      const decoded = decodeURIComponent(options.q);
      if (decoded.includes('code=')) return decoded.split('code=')[1].split('&')[0];
      return decoded;
    }
    return '';
  },

  /**
   * 自动绑定桌台（扫码场景，不需要登录）
   * @param {string} code - 桌号编码
   */
  async autoBind(code) {
    if (!code) return;
    const requestCode = normalizeTableCode(code);
    try {
      const { table: boundTable } = await bindTableByCode(code);
      if (requestCode !== normalizeTableCode(this.data.tableCode)) {
        return;
      }
      this.setData({ table: boundTable, tableCode: boundTable.code || code, loading: false, sceneRefreshPending: false }, () => {
        this.refreshHeroState();
      });
      wx.showToast({
        title: `已进入桌台 ${boundTable.code || boundTable.name}`,
        icon: 'none',
        duration: 1200
      });
      this.tryRedirectToMenu();
    } catch (err) {
      this.setData({ loading: false, sceneRefreshPending: false }, () => {
        this.refreshHeroState();
      });
      wx.showToast({ title: err.message || '桌台不存在', icon: 'none' });
    }
  },

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
      const reboundTable = await ensureCurrentUserTableBinding(this.data.table || get(KEYS.TABLE));
      this.setData({ loggedIn: true, showLoginPanel: false, table: reboundTable || this.data.table || null }, () => {
        this.refreshHeroState();
      });
      await this.loadCouponPreview();
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

  toggleLoginPanel() {
    this.setData({
      showLoginPanel: !this.data.showLoginPanel,
      agreeProtocol: false
    });
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

  tryRedirectToMenu() {
    const { sceneEntry, autoRedirected, table } = this.data;
    if (!sceneEntry || autoRedirected || !table || !table.id) return;

    this.setData({ autoRedirected: true });
    setTimeout(() => {
      wx.switchTab({ url: '/pages/menu/index' });
    }, 150);
  },

  goMenu() {
    if (!this.data.table) {
      wx.navigateTo({ url: '/pages/table/index' });
      return;
    }
    wx.switchTab({ url: '/pages/menu/index' });
  },

  goOrder() {
    if (!isLoggedIn()) {
      this.setData({ showLoginPanel: true });
      return;
    }
    wx.switchTab({ url: '/pages/order/index' });
  },

  goCouponCenter() {
    if (!isLoggedIn()) {
      this.setData({ showLoginPanel: true });
      return;
    }
    wx.navigateTo({ url: '/pages/coupon/index' });
  },

  goTablePage() {
    wx.navigateTo({ url: '/pages/table/index' });
  },

  openCategory() {
    wx.switchTab({ url: '/pages/menu/index' });
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

  noop() {}
});
