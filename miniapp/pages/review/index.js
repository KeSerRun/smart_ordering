const reviewApi = require('../../api/review');
const orderApi = require('../../api/order');
const { KEYS, get, set } = require('../../utils/storage');

function normalizeId(v) {
  if (v === null || v === undefined) return '';
  const s = String(v).trim();
  return s && s !== '0' ? s : '';
}

function pickItemId(item) {
  if (!item || typeof item !== 'object') return '';
  return normalizeId(item.id ?? item.itemId ?? item.orderItemId);
}

function isMockPaidOrder(orderId) {
  const list = get(KEYS.MOCK_PAID_ORDER_IDS) || [];
  const ids = Array.isArray(list) ? list.map(v => normalizeId(v)) : [];
  return ids.includes(normalizeId(orderId));
}

function canReview(order, orderId) {
  if (!order) return false;
  if (Number(order.status) === 1) return true;
  return isMockPaidOrder(orderId);
}

function markOrderReviewed(orderId) {
  const raw = get(KEYS.REVIEWED_ORDER_IDS) || [];
  const list = Array.isArray(raw) ? raw : [];
  const id = normalizeId(orderId);
  if (!id) return;
  const next = Array.from(new Set([...list.map((v) => normalizeId(v)), id]));
  set(KEYS.REVIEWED_ORDER_IDS, next);
}

Page({
  data: {
    orderId: '',
    order: null,
    overallRating: 5,
    content: '',
    itemRatings: []
  },

  onLoad(query) {
    const orderId = normalizeId(query.orderId || get(KEYS.ORDER_ID));
    this.setData({ orderId });
    if (!orderId) {
      wx.showToast({ title: '未找到订单号', icon: 'none' });
      return;
    }
    this.loadOrder(orderId);
  },

  async loadOrder(id) {
    try {
      const existed = await reviewApi.getOrderReview(id);
      if (existed && existed.id) {
        wx.showToast({ title: '该订单已评价', icon: 'none' });
        setTimeout(() => wx.navigateBack({ delta: 1 }), 400);
        return;
      }

      const order = await orderApi.getOrder(id);
      if (!canReview(order, id)) {
        wx.showToast({ title: '请先完成支付再评价', icon: 'none' });
        setTimeout(() => wx.navigateBack({ delta: 1 }), 400);
        return;
      }

      const itemRatings = (order.items || []).map((it) => ({
        orderItemId: pickItemId(it),
        rating: 5,
        dishName: it.dishName
      }));
      this.setData({ order, itemRatings });
    } catch (err) {
      wx.showToast({ title: err.message || '获取订单失败', icon: 'none' });
    }
  },

  onOverallChange(e) {
    this.setData({ overallRating: e.detail.value });
  },

  onItemChange(e) {
    const orderItemId = normalizeId(e.currentTarget.dataset.id);
    const rating = e.detail.value;
    const itemRatings = this.data.itemRatings.map((it) =>
      it.orderItemId === orderItemId ? { ...it, rating } : it
    );
    this.setData({ itemRatings });
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value });
  },

  async submit() {
    if (!this.data.orderId) {
      wx.showToast({ title: '订单号无效', icon: 'none' });
      return;
    }

    if (!canReview(this.data.order, this.data.orderId)) {
      wx.showToast({ title: '请先完成支付再评价', icon: 'none' });
      return;
    }

    const payload = {
      orderId: this.data.orderId,
      overallRating: this.data.overallRating,
      content: this.data.content,
      itemRatings: this.data.itemRatings
        .filter((it) => !!it.orderItemId)
        .map((it) => ({ orderItemId: it.orderItemId, rating: it.rating }))
    };

    if (isMockPaidOrder(this.data.orderId)) {
      await new Promise((resolve) => setTimeout(resolve, 300));
      markOrderReviewed(this.data.orderId);
      wx.navigateTo({ url: '/pages/result/index?ok=1&type=review' });
      return;
    }

    try {
      const existed = await reviewApi.getOrderReview(this.data.orderId);
      if (existed && existed.id) {
        wx.showToast({ title: '该订单已评价', icon: 'none' });
        return;
      }
      await reviewApi.submitReview(payload);
      markOrderReviewed(this.data.orderId);
      wx.navigateTo({ url: '/pages/result/index?ok=1&type=review' });
    } catch (err) {
      wx.showToast({ title: err.message || '提交评价失败', icon: 'none' });
    }
  }
});
