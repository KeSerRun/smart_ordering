const feedbackApi = require('../../api/feedback');
const { KEYS, get } = require('../../utils/storage');

function normalizeList(list) {
  if (!Array.isArray(list)) return [];
  return list.map(item => ({
    ...item,
    statusText: Number(item.status) === 1 ? '已回复' : '待回复'
  }));
}

Page({
  data: {
    content: '',
    contactPhone: '',
    loading: false,
    submitting: false,
    pageNum: 1,
    pageSize: 20,
    total: 0,
    list: []
  },

  onShow() {
    const userInfo = get(KEYS.USER_INFO) || {};
    this.setData({
      contactPhone: userInfo.phone || userInfo.mobile || this.data.contactPhone || ''
    });
    this.loadData();
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value });
  },

  onPhoneInput(e) {
    this.setData({ contactPhone: e.detail.value.trim() });
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const result = await feedbackApi.getMyFeedback(this.data.pageNum, this.data.pageSize);
      this.setData({
        list: normalizeList(result.list || []),
        total: Number(result.total || 0)
      });
    } catch (err) {
      wx.showToast({ title: err.message || '加载反馈失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  async handleSubmit() {
    const content = (this.data.content || '').trim();
    if (!content) {
      wx.showToast({ title: '请先填写反馈内容', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    try {
      await feedbackApi.submitFeedback({
        content,
        contactPhone: this.data.contactPhone || ''
      });
      wx.showToast({ title: '反馈已提交', icon: 'none' });
      this.setData({ content: '' });
      await this.loadData();
    } catch (err) {
      wx.showToast({ title: err.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
