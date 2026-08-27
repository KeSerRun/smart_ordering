const reviewApi = require('../../api/review');
const orderApi = require('../../api/order');
const { env } = require('../../config/env');

function normalizeId(v) {
  if (v === null || v === undefined) return '';
  return String(v);
}

function normalizeImageUrl(raw) {
  if (!raw) return '';
  let url = String(raw).trim();
  if (!url) return '';
  if (/^data:image\//i.test(url)) return url;
  if (/^https?:\/\//i.test(url)) return url;
  if (url.startsWith('//')) return `https:${url}`;
  if (url.startsWith('/pages/')) return '';
  if (url.startsWith('/')) {
    if (url.startsWith('/api/')) return `${env.apiHost}${url}`;
    return `${env.apiHost}/api${url}`;
  }
  return `${env.apiHost}/${url}`;
}

function normalizeRows(rows) {
  return (rows || []).map((row) => ({
    ...row,
    itemReviews: Array.isArray(row.itemReviews) ? row.itemReviews : [],
    itemReviewDisplay: []
  }));
}

async function enrichRows(rows) {
  const tasks = (rows || []).map(async (row) => {
    const orderId = row.orderId;
    let itemMap = {};
    let orderNo = '';
    try {
      const order = await orderApi.getOrder(orderId);
      orderNo = order && order.orderNo ? String(order.orderNo) : '';
      const items = (order && order.items) || [];
      items.forEach((it) => {
        itemMap[normalizeId(it.id ?? it.itemId ?? it.orderItemId)] = {
          dishName: it.dishName || '',
          dishImageUrl: normalizeImageUrl(it.dishImage || it.image || it.thumbnail)
        };
      });
    } catch (err) {
      itemMap = {};
    }

    const itemReviewDisplay = (row.itemReviews || []).map((it) => {
      const itemId = normalizeId(it.orderItemId);
      const matched = itemMap[itemId] || {};
      const dishName = matched.dishName || '';
      const ratingValue = Math.max(0, Math.min(5, Number(it.rating ?? it.score ?? 0) || 0));
      return {
        ...it,
        itemId,
        dishName,
        dishImageUrl: matched.dishImageUrl || '',
        imageError: false,
        ratingValue,
        displayName: dishName || `订单项 ${itemId}`
      };
    });

    return {
      ...row,
      orderNo: orderNo || normalizeId(orderId),
      itemReviewDisplay
    };
  });

  return Promise.all(tasks);
}

Page({
  data: {
    loading: false,
    finished: false,
    pageNum: 1,
    pageSize: 20,
    total: 0,
    list: [],
    stars: [1, 2, 3, 4, 5]
  },

  onItemImageError(e) {
    const reviewId = String(e.currentTarget.dataset.reviewId || '');
    const itemId = String(e.currentTarget.dataset.itemId || '');
    if (!reviewId || !itemId) return;

    const list = (this.data.list || []).map((review) => {
      if (String(review.id) !== reviewId) return review;
      const itemReviewDisplay = (review.itemReviewDisplay || []).map((item) =>
        String(item.itemId || item.orderItemId || '') === itemId ? { ...item, imageError: true } : item
      );
      return { ...review, itemReviewDisplay };
    });

    this.setData({ list });
  },

  onShow() {
    this.reload();
  },

  onReachBottom() {
    this.loadMore();
  },

  async reload() {
    this.setData({
      loading: false,
      finished: false,
      pageNum: 1,
      total: 0,
      list: []
    });
    await this.loadMore();
  },

  async loadMore() {
    if (this.data.loading || this.data.finished) return;

    this.setData({ loading: true });
    try {
      const result = await reviewApi.getMyReviews(this.data.pageNum, this.data.pageSize);
      const rows = await enrichRows(normalizeRows((result && result.list) || []));
      const total = Number((result && result.total) || 0);
      const next = this.data.list.concat(rows);
      const finished = next.length >= total || rows.length < this.data.pageSize;

      this.setData({
        list: next,
        total,
        pageNum: this.data.pageNum + 1,
        finished
      });
    } catch (err) {
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  }
});
