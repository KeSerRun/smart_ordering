<template>
  <view class="order-page">
    <app-navbar title="订单" backgroundColor="#fcfaf7" />
    <view class="order-page__glow order-page__glow-a"></view>
    <view class="order-page__glow order-page__glow-b"></view>

    <view class="order-topbar">
      <view class="order-topbar__hint">{{ tableId ? ('桌号 ' + tableId) : '当前桌台' }}</view>
    </view>

    <view class="order-stats" v-if="orders.length > 0">
      <view class="order-stats__card">
        <text class="order-stats__label">订单数</text>
        <text class="order-stats__value">{{ orders.length }}</text>
      </view>
      <view class="order-stats__card">
        <text class="order-stats__label">待支付</text>
        <text class="order-stats__value">{{ unpaidOrderCount }}</text>
      </view>
      <view class="order-stats__card">
        <text class="order-stats__label">已完成菜品</text>
        <text class="order-stats__value">{{ completedDishCount }}</text>
      </view>
    </view>

    <view class="order-section" v-if="orders.length > 0">
      <view class="order-card" v-for="order in orders" :key="order.id">
        <view class="order-card__head">
          <view class="order-card__meta">
            <view class="order-card__store">堂食 · 当前桌台</view>
            <view class="order-card__time">{{ order.timeText }}</view>
          </view>
          <view class="order-card__status" :class="'status-' + order.status">{{ order.statusText }}</view>
        </view>

        <view class="order-card__body">
          <view class="order-card__items">
            <view class="order-card__item" v-for="dish in order.items" :key="dish.id">
              <view class="order-card__item-info">
                <view class="order-card__item-top">
                  <text class="order-card__item-name">{{ dish.dishName }}</text>
                  <text class="order-card__item-price">¥{{ dish.price }}</text>
                </view>
                <view class="order-card__item-sub">{{ dish.remark || '默认口味' }} · x{{ dish.quantity }}</view>
              </view>
              <view class="order-card__item-state" :class="'state-' + dish.status">{{ dish.statusText }}</view>
            </view>
          </view>

          <view class="order-card__total">
            <view class="order-card__total-row">
              <text>菜品数量</text>
              <text>共 {{ order.items.length }} 件</text>
            </view>
            <view class="order-card__total-row order-card__total-row--strong">
              <text>合计</text>
              <text>¥{{ order.actualAmount }}</text>
            </view>
          </view>
        </view>

        <view class="order-card__foot">
          <button class="order-card__action primary pay-action" v-if="order.status === 0" @tap="goPayment(order.id)">去支付</button>
          <button class="order-card__action primary" v-if="order.status === 1 && !order.reviewed" @tap="goReview(order, order.status)">去评价</button>
          <view class="order-card__done" v-if="order.reviewed">已评价</view>
        </view>
      </view>
    </view>

    <view v-else class="order-empty">
      <image class="order-empty__icon" src="/static/tabbar/order.png" mode="aspectFit" />
      <view class="order-empty__title">还没有订单</view>
      <view class="order-empty__desc">先去点餐页选菜，下单后这里会展示支付和出餐进度。</view>
      <button class="order-empty__btn" @tap="goMenuForAddItem">去点餐</button>
    </view>
  </view>
</template>

<script setup>
// 订单页 / 我的订单
import { reactive, toRefs } from 'vue';
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app';
import { getTableOrders, rushItem } from '@/api/order';
import { getOrderReview } from '@/api/review';
import { addSocketListener, connectSocket } from '@/utils/socket';
import { KEYS, get, set, getTableBindingKey } from '@/utils/storage';

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
    (Array.isArray(reviewedLocal) ? reviewedLocal : []).map((v) => normalizeId(v)).filter((v) => !!v)
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

// ===== 状态 =====
const state = reactive({
  tableId: null,
  tableBindingKey: '',
  orders: [],
  paidOrderCount: 0,
  unpaidOrderCount: 0,
  completedDishCount: 0
});

const { tableId, orders, unpaidOrderCount, completedDishCount } = toRefs(state);

let pollTimer = null;
let unsubscribe = null;

onLoad(() => {
  const table = get(KEYS.TABLE) || {};
  resetOrderState(table);
});

onShow(() => {
  const table = get(KEYS.TABLE) || {};
  const tableId = Number(table.id || 0);
  const bindingKey = getTableBindingKey(table);
  if (bindingKey !== state.tableBindingKey) {
    resetOrderState(table);
  }
  if (!tableId) {
    stopPolling();
    return;
  }
  loadOrders();
  connectSocket();
  unsubscribe = addSocketListener((msg) => {
    if (msg && (msg.eventType === 'ITEM_COMPLETED' || msg.eventType === 'ALL_COMPLETED')) {
      loadOrders();
    }
  });
  startPolling();
});

onHide(() => {
  stopPolling();
  if (unsubscribe) unsubscribe();
});

onUnload(() => {
  stopPolling();
  if (unsubscribe) unsubscribe();
});

function startPolling() {
  stopPolling();
  pollTimer = setInterval(() => loadOrders(), 8000);
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
}

function resetOrderState(table = {}) {
  state.tableId = Number(table.id || 0);
  state.tableBindingKey = getTableBindingKey(table);
  state.orders = [];
  state.paidOrderCount = 0;
  state.unpaidOrderCount = 0;
  state.completedDishCount = 0;
}

async function loadOrders() {
  const currentTableId = Number(state.tableId || 0);
  const currentBindingKey = state.tableBindingKey;
  if (!currentTableId) return;
  try {
    const mockPaid = get(KEYS.MOCK_PAID_ORDER_IDS) || [];
    const mockPaidSet = new Set((Array.isArray(mockPaid) ? mockPaid : []).map((v) => String(v)));
    const reviewedSet = getReviewedSet();

    const list = await getTableOrders(currentTableId);
    if (currentTableId !== Number(state.tableId || 0) || currentBindingKey !== state.tableBindingKey) return;

    const nextOrders = (list || [])
      .map((o) => normalizeOrder(o, mockPaidSet, reviewedSet))
      .filter((o) => !!o.id);
    state.orders = nextOrders;
    state.paidOrderCount = nextOrders.filter((item) => Number(item.status) === 1).length;
    state.unpaidOrderCount = nextOrders.filter((item) => Number(item.status) === 0).length;
    state.completedDishCount = nextOrders.reduce((sum, order) => {
      return sum + (order.items || []).filter((dish) => Number(dish.status) === 2).length;
    }, 0);
  } catch (err) {
    uni.showToast({ title: err.message || '加载订单失败', icon: 'none' });
  }
}

function goMenuForAddItem() {
  uni.switchTab({ url: '/pages/menu/index' });
}

function goPayment(orderId) {
  if (!orderId) {
    uni.showToast({ title: '订单无效', icon: 'none' });
    return;
  }
  set(KEYS.ORDER_ID, orderId);
  uni.navigateTo({ url: `/pages/payment/index?orderId=${orderId}` });
}

function goMyReviews() {
  uni.navigateTo({ url: '/pages/my-review/index' });
}

async function goReview(order, status) {
  const orderId = pickId(order) || '';
  if (!orderId) {
    uni.showToast({ title: '订单无效', icon: 'none' });
    return;
  }
  if (status !== 1) {
    uni.showToast({ title: '请先完成支付再评价', icon: 'none' });
    return;
  }
  const reviewedSet = getReviewedSet();
  if (reviewedSet.has(normalizeId(orderId))) {
    uni.showToast({ title: '该订单已评价', icon: 'none' });
    return;
  }
  try {
    const review = await getOrderReview(orderId);
    if (review && review.id) {
      reviewedSet.add(normalizeId(orderId));
      saveReviewedSet(reviewedSet);
      loadOrders();
      uni.showToast({ title: '该订单已评价', icon: 'none' });
      return;
    }
  } catch (err) {
    // ignore and continue to review page
  }
  set(KEYS.ORDER_ID, orderId);
  uni.navigateTo({ url: `/pages/review/index?orderId=${orderId}` });
}

async function rushItemById(orderId, itemId) {
  if (!orderId || !itemId) {
    uni.showToast({ title: '订单或菜品标识无效', icon: 'none' });
    return;
  }
  try {
    await rushItem(orderId, itemId);
    uni.showToast({ title: '已发送催单', icon: 'none' });
  } catch (err) {
    uni.showToast({ title: err.message || '催单失败', icon: 'none' });
  }
}
</script>

<style scoped>
.order-page {
  --order-red: #07c160;
  --order-red-deep: #057a3d;
  --order-cream: #f7f3ee;
  --order-card: #fffdfb;
  --order-line: rgba(7, 193, 96, 0.08);
  --order-text: #251819;
  --order-subtext: #8d7e78;

  min-height: 100vh;
  position: relative;
  background:
    radial-gradient(circle at top right, rgba(7, 193, 96, 0.08), transparent 28%),
    linear-gradient(180deg, #fcfaf7 0%, var(--order-cream) 42%, #efe7df 100%);
  padding: 22rpx 22rpx 180rpx;
  box-sizing: border-box;
}

.order-page__glow {
  position: absolute;
  border-radius: 999rpx;
  pointer-events: none;
  filter: blur(12rpx);
}

.order-page__glow-a {
  top: 90rpx;
  right: -100rpx;
  width: 260rpx;
  height: 260rpx;
  background: rgba(7, 193, 96, 0.12);
}

.order-page__glow-b {
  left: -120rpx;
  bottom: 120rpx;
  width: 320rpx;
  height: 320rpx;
  background: rgba(206, 175, 127, 0.14);
}

.order-topbar {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 16rpx;
  padding: 6rpx 4rpx 4rpx;
}

.order-topbar__hint {
  flex-shrink: 0;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  border: 1rpx solid rgba(7, 193, 96, 0.1);
  background: rgba(255, 255, 255, 0.8);
  color: #07c160;
  font-size: 20rpx;
  line-height: 1;
  text-align: center;
  box-shadow: 0 8rpx 18rpx rgba(55, 84, 48, 0.04);
}

.order-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12rpx;
  margin-top: 14rpx;
}

.order-stats__card {
  padding: 18rpx 14rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 14rpx 28rpx rgba(55, 84, 48, 0.06);
  text-align: center;
}

.order-stats__label {
  display: block;
  color: var(--order-subtext);
  font-size: 20rpx;
}

.order-stats__value {
  display: block;
  margin-top: 10rpx;
  color: var(--order-text);
  font-size: 32rpx;
  font-weight: 800;
}

.order-section {
  margin-top: 18rpx;
}

.order-card {
  position: relative;
  z-index: 2;
  margin-bottom: 16rpx;
  padding: 22rpx;
  border-radius: 28rpx;
  background: var(--order-card);
  box-shadow: 0 16rpx 30rpx rgba(55, 84, 48, 0.06);
}

.order-card__head,
.order-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.order-card__meta {
  min-width: 0;
  flex: 1;
}

.order-card__store {
  color: var(--order-text);
  font-size: 28rpx;
  font-weight: 700;
}

.order-card__time {
  margin-top: 8rpx;
  color: var(--order-subtext);
  font-size: 21rpx;
}

.order-card__status {
  flex-shrink: 0;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 700;
}

.status-0,
.state-0 {
  color: #9f6e12;
  background: #f8efdd;
}

.status-1,
.state-1 {
  color: #3f6b34;
  background: #edf5e8;
}

.status-2,
.state-2 {
  color: #7a7d80;
  background: #eff1f4;
}

.order-card__body {
  margin-top: 18rpx;
}

.order-card__items {
  margin-top: 18rpx;
  border-top: 1rpx solid var(--order-line);
  border-bottom: 1rpx solid var(--order-line);
}

.order-card__item {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 16rpx 0;
}

.order-card__item + .order-card__item {
  border-top: 1rpx solid rgba(7, 193, 96, 0.06);
}

.order-card__item-info {
  flex: 1;
  min-width: 0;
}

.order-card__item-top {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.order-card__item-name {
  flex: 1;
  min-width: 0;
  color: var(--order-text);
  font-size: 25rpx;
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-card__item-price {
  flex-shrink: 0;
  color: var(--order-text);
  font-size: 24rpx;
  font-weight: 800;
}

.order-card__item-sub {
  margin-top: 6rpx;
  color: var(--order-subtext);
  font-size: 21rpx;
  line-height: 1.4;
}

.order-card__item-state {
  flex-shrink: 0;
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  font-size: 19rpx;
  font-weight: 800;
}

.order-card__total {
  padding-top: 14rpx;
}

.order-card__total-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  color: var(--order-subtext);
  font-size: 23rpx;
  line-height: 1.5;
}

.order-card__total-row + .order-card__total-row {
  margin-top: 8rpx;
}

.order-card__total-row--strong {
  color: var(--order-text);
  font-size: 30rpx;
  font-weight: 900;
}

.order-card__foot {
  margin-top: 18rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid var(--order-line);
  justify-content: flex-end;
}

.order-card__action,
.order-empty__btn {
  height: 72rpx;
  line-height: 72rpx;
  margin: 0;
  border-radius: 24rpx;
  padding: 0 24rpx;
  font-size: 24rpx;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.order-card__action::after,
.order-empty__btn::after {
  border: none;
}

.order-card__action {
  flex: 1;
}

.order-card__action.ghost {
  color: #07a857;
  background: linear-gradient(180deg, #f5fcf8 0%, #e8f8ee 100%);
  border: 1rpx solid rgba(7, 193, 96, 0.18);
}

.order-card__action.primary,
.order-empty__btn {
  color: #ffffff;
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  border: 1rpx solid rgba(7, 193, 96, 0.28);
  box-shadow: 0 12rpx 26rpx rgba(7, 193, 96, 0.18);
}

.order-card__action.primary.pay-action {
  color: #ffffff;
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  border: 1rpx solid rgba(7, 193, 96, 0.28);
  box-shadow: 0 12rpx 26rpx rgba(7, 193, 96, 0.18);
}

.order-card__done {
  color: var(--order-subtext);
  font-size: 22rpx;
}

.order-empty {
  position: relative;
  z-index: 2;
  margin-top: 22rpx;
  padding: 70rpx 36rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.92);
  text-align: center;
}

.order-empty__icon {
  width: 150rpx;
  height: 150rpx;
  opacity: 0.5;
}

.order-empty__title {
  margin-top: 24rpx;
  color: var(--order-text);
  font-size: 34rpx;
  font-weight: 800;
}

.order-empty__desc {
  margin-top: 14rpx;
  color: var(--order-subtext);
  font-size: 24rpx;
  line-height: 1.7;
}

.order-empty__btn {
  width: 100%;
  margin-top: 28rpx;
}
</style>