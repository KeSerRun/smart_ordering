const { KEYS, get } = require('../../utils/storage');
const { isLoggedIn, wxLogin, phoneLogin } = require('../../utils/auth');
const { bindTableByCode, previewTableByCode, ensureCurrentUserTableBinding, normalizeTableCode } = require('../../utils/table-binding');

Page({
  data: {
    tableCode: '',
    table: null,
    loggedIn: false,
    autoLoadFromSceneDone: false,
    agreeProtocol: false,
    entryHintText: '',
    entryHintTone: ''
  },

  onLoad(options) {
    const code = this.extractCodeFromOptions(options || {});
    if (!code) return;
    this.setData({ tableCode: code, autoLoadFromSceneDone: false });
  },

  onShow() {
    this.setData({ loggedIn: isLoggedIn() });

    const cachedTable = get(KEYS.TABLE);
    const sceneTableCode = normalizeTableCode(this.data.tableCode);
    const cachedTableCode = normalizeTableCode(cachedTable && cachedTable.code);
    if (sceneTableCode && !this.data.autoLoadFromSceneDone) {
      this.setData({
        table: null,
        autoLoadFromSceneDone: true,
        entryHintText: '',
        entryHintTone: ''
      });
      this.loadTable(this.data.tableCode);
      return;
    }

    if (cachedTable) {
      this.setData({ table: cachedTable, tableCode: cachedTable.code || '' });
      return;
    }

    if (this.data.tableCode && !this.data.autoLoadFromSceneDone) {
      this.setData({ autoLoadFromSceneDone: true });
      this.loadTable();
    }
  },

  onCodeInput(e) {
    this.setData({ tableCode: e.detail.value.trim() });
  },

  /**
   * 手机号登录：open-type="getPhoneNumber" 的回调
   */
  async handlePhoneLogin(e) {
    if (!this.data.agreeProtocol) {
      wx.showToast({ title: '请先勾选用户协议与隐私政策', icon: 'none' });
      return;
    }
    if (!e.detail.code) {
      wx.showToast({ title: '请授权手机号登录', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '登录中', mask: true });
    try {
      const code = await wxLogin();
      await phoneLogin(code, e.detail.code);
      const cachedTable = get(KEYS.TABLE);
      const reboundTable = await ensureCurrentUserTableBinding(cachedTable);
      this.setData({
        loggedIn: true,
        table: reboundTable || this.data.table || null,
        tableCode: (reboundTable || this.data.table || {}).code || this.data.tableCode
      });
      wx.showToast({ title: '登录成功', icon: 'none' });
    } catch (err) {
      wx.showToast({ title: err.message || '登录失败', icon: 'none' });
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

  async scanCode() {
    wx.scanCode({
      onlyFromCamera: true,
      success: async (res) => {
        const content = res.result || '';
        const parsedCode = this.parseCode(content);
        this.setData({ tableCode: parsedCode });
        await this.loadTable();
      },
      fail: () => {
        wx.showToast({ title: '扫码失败', icon: 'none' });
      }
    });
  },

  parseCode(content) {
    if (!content) return '';
    if (content.includes('code=')) {
      const parts = content.split('code=');
      return decodeURIComponent(parts[1].split('&')[0]);
    }
    return content;
  },

  extractCodeFromOptions(options) {
    if (options.scene) {
      const decodedScene = decodeURIComponent(options.scene);
      const sceneCode = this.parseCode(decodedScene);
      if (sceneCode) return sceneCode;
    }
    if (options.code) {
      return this.parseCode(String(options.code));
    }
    if (options.q) {
      const decodedQ = decodeURIComponent(options.q);
      return this.parseCode(decodedQ);
    }
    return '';
  },

  async loadTable() {
    const tableCode = this.data.tableCode;
    if (!tableCode) {
      wx.showToast({ title: '请输入桌号编码', icon: 'none' });
      return;
    }
    const requestCode = normalizeTableCode(tableCode);
    this.setData({ entryHintText: '', entryHintTone: '' });
    wx.showLoading({ title: '加载中' });
    try {
      const previewTable = await previewTableByCode(tableCode);
      if (requestCode !== normalizeTableCode(this.data.tableCode)) {
        return;
      }
      const entryHint = this.buildEntryHint(Number(previewTable.status));
      this.setData({
        table: previewTable,
        tableCode: previewTable.code || tableCode,
        entryHintText: entryHint.text,
        entryHintTone: entryHint.tone
      });
    } catch (err) {
      wx.showToast({ title: err.message || '获取桌台失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  buildEntryHint(status) {
    if (status === 0) {
      return {
        tone: 'info',
        text: '桌台当前空闲，进入点餐后会自动开台。'
      };
    }

    if (status === 1) {
      return {
        tone: 'warm',
        text: '该桌已有进行中点单，进入后可继续加菜或支付。'
      };
    }

    return {
      tone: '',
      text: ''
    };
  },

  async enterMenu() {
    const { table } = this.data;
    if (!table || !table.id) {
      wx.showToast({ title: '请先关联桌台', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '进入点餐', mask: true });
    try {
      const { table: boundTable } = await bindTableByCode(table.code || this.data.tableCode);
      this.setData({ table: boundTable, tableCode: boundTable.code || this.data.tableCode });
      wx.switchTab({ url: '/pages/menu/index' });
    } catch (err) {
      wx.showToast({ title: err.message || '进入点餐失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  }
});
