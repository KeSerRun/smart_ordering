const orderApi = require('../../api/order');
const reviewApi = require('../../api/review');
const { addSocketListener, connectSocket } = require('../../utils/socket');
const { KEYS, get, set, getTableBindingKey } = require('../../utils/storage');

function pickId(obj) {
  if (!obj || typeof obj !== 'object') return '';
  const raw = obj.id ?? obj.orderId ?? '';
  return raw === null || raw === undefined ? '' : String(raw);
}

function normalizeId(v) {
  if (v === null || v === undefined) return '';
  const s = String(v).trim();
  return s && s !== '0' ? s : '';
}

function pickItemId(obj) {
  if (!obj || typeof obj !== 'object') return '';
  const raw = obj.id ?? obj.itemId ?? obj.orderItemId ?? '';
  return raw === null || raw === undefined ? '' : String(raw);
}

function mapOrderStatus(status) {
  if (status === 0) return '待支付';
  if (status === 1) return '已支付';
  return '已取消';
}

function mapItemStatus(status) {
  if (status === 0) return '待制作';
  if (status === 1) return '制作中';
  return '已完成';
}

function formatShortTime(v) {
  if (!v) return '--';
  const s = String(v).replace('T', ' ');
  return s.length >= 16 ? s.slice(0, 16) : s;
}

function getReviewedSet() {
  const reviewedLocal = get(KEYS.REVIEWED_ORDER_IDS) || [];
  return new Set(
    (Array.isArray(reviewedLocal) ? reviewedLocal : [])
      .map((v) => normalizeId(v))
      .filter((v) => !!v)
  );
}

function saveReviewedSet(setObj) {
  set(KEYS.REVIEWED_ORDER_IDS, Array.from(setObj));
}

function normalizeOrder(order, mockPaidSet, reviewedSet) {
  const id = pickId(order);
  let status = order.status;

  if (id && status === 0 && mockPaidSet.has(id)) {
    status = 1;
  }

  return {
    ...order,
    id,
    status,
    reviewed: reviewedSet.has(normalizeId(id)),
    statusText: mapOrderStatus(status),
    timeText: formatShortTime(order.createTime || order.createdTime || order.orderTime),
    items: (order.items || []).map((it) => ({
      ...it,
      id: pickItemId(it),
      statusText: mapItemStatus(it.status)
    }))
  };
}

Page({
  data: {
    tableId: null,
    tableBindingKey: '',
    orders: [],
    paidOrderCount: 0,
    unpaidOrderCount: 0,
    completedDishCount: 0,
    pollTimer: null
  },

  onLoad() {
    const table = get(KEYS.TABLE) || {};
    this.resetOrderState(table);
  },

  onShow() {
    const table = get(KEYS.TABLE) || {};
    const tableId = Number(table.id || 0);
    const tableBindingKey = getTableBindingKey(table);
    if (tableBindingKey !== this.data.tableBindingKey) {
      this.resetOrderState(table);
    }
    if (!tableId) {
      this.stopPolling();
      return;
    }

    this.loadOrders();
    connectSocket();
    this.unsubscribe = addSocketListener((msg) => {
      if (msg && (msg.eventType === 'ITEM_COMPLETED' || msg.eventType === 'ALL_COMPLETED')) {
        this.loadOrders();
      }
    });
    this.startPolling();
  },

  onHide() {
    this.stopPolling();
    if (this.unsubscribe) this.unsubscribe();
  },

  onUnload() {
    this.stopPolling();
    if (this.unsubscribe) this.unsubscribe();
  },

  startPolling() {
    this.stopPolling();
    const timer = setInterval(() => this.loadOrders(), 8000);
    this.setData({ pollTimer: timer });
  },

  stopPolling() {
    if (this.data.pollTimer) {
      clearInterval(this.data.pollTimer);
      this.setData({ pollTimer: null });
    }
  },

  resetOrderState(table = {}) {
    this.setData({
      tableId: Number(table.id || 0),
      tableBindingKey: getTableBindingKey(table),
      orders: [],
      paidOrderCount: 0,
      unpaidOrderCount: 0,
      completedDishCount: 0
    });
  },

  async loadOrders() {
    const currentTableId = Number(this.data.tableId || 0);
    const currentBindingKey = this.data.tableBindingKey;
    if (!currentTableId) return;
    try {
      const mockPaid = get(KEYS.MOCK_PAID_ORDER_IDS) || [];
      const mockPaidSet = new Set((Array.isArray(mockPaid) ? mockPaid : []).map((v) => String(v)));
      const reviewedSet = getReviewedSet();

      const list = await orderApi.getTableOrders(currentTableId);
      if (currentTableId !== Number(this.data.tableId || 0) || currentBindingKey !== this.data.tableBindingKey) {
        return;
      }

      const orders = (list || [])
        .map((o) => normalizeOrder(o, mockPaidSet, reviewedSet))
        .filter((o) => !!o.id);
      const nextOrders = orders;
      const paidOrderCount = nextOrders.filter((item) => Number(item.status) === 1).length;
      const unpaidOrderCount = nextOrders.filter((item) => Number(item.status) === 0).length;
      const completedDishCount = nextOrders.reduce((sum, order) => {
        return sum + (order.items || []).filter((dish) => Number(dish.status) === 2).length;
      }, 0);

      this.setData({
        orders: nextOrders,
        paidOrderCount,
        unpaidOrderCount,
        completedDishCount
      });
    } catch (err) {
      wx.showToast({ title: err.message || '加载订单失败', icon: 'none' });
    }
  },

  async rushItem(e) {
    const orderId = String(e.currentTarget.dataset.orderId || '');
    const itemId = String(e.currentTarget.dataset.itemId || '');
    if (!orderId || !itemId) {
      wx.showToast({ title: '订单或菜品标识无效', icon: 'none' });
      return;
    }

    try {
      await orderApi.rushItem(orderId, itemId);
      wx.showToast({ title: '已发送催单', icon: 'none' });
    } catch (err) {
      wx.showToast({ title: err.message || '催单失败', icon: 'none' });
    }
  },

  goMenuForAddItem() {
    wx.switchTab({ url: '/pages/menu/index' });
  },

  goPayment(e) {
    const orderId = String(e.currentTarget.dataset.orderId || '');
    if (!orderId) {
      wx.showToast({ title: '订单无效', icon: 'none' });
      return;
    }
    set(KEYS.ORDER_ID, orderId);
    wx.navigateTo({ url: `/pages/payment/index?orderId=${orderId}` });
  },

  goMyReviews() {
    wx.navigateTo({ url: '/pages/my-review/index' });
  },

  async goReview(e) {
    const orderId = String(e.currentTarget.dataset.orderId || '');
    const status = Number(e.currentTarget.dataset.status);
    if (!orderId) {
      wx.showToast({ title: '订单无效', icon: 'none' });
      return;
    }
    if (status !== 1) {
      wx.showToast({ title: '请先完成支付再评价', icon: 'none' });
      return;
    }

    const reviewedSet = getReviewedSet();
    if (reviewedSet.has(normalizeId(orderId))) {
      wx.showToast({ title: '该订单已评价', icon: 'none' });
      return;
    }

    try {
      const review = await reviewApi.getOrderReview(orderId);
      if (review && review.id) {
        reviewedSet.add(normalizeId(orderId));
        saveReviewedSet(reviewedSet);
        this.loadOrders();
        wx.showToast({ title: '该订单已评价', icon: 'none' });
        return;
      }
    } catch (err) {
      // ignore and continue to review page
    }

    set(KEYS.ORDER_ID, orderId);
    wx.navigateTo({ url: `/pages/review/index?orderId=${orderId}` });
  }
});
