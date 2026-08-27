<template>
  <view class="page">
    <app-navbar title="用餐评价" />
    <view class="page-glow page-glow-a"></view>
    <view class="page-glow page-glow-b"></view>

    <view class="review-header">
      <view class="header-chip">用餐评价</view>
      <view class="header-title">这顿吃得怎么样</view>
      <view class="header-desc">整体体验和单品口味都可以直接反馈，后厨和门店会据此优化。</view>
    </view>

    <view class="section-card">
      <view class="section-label">整体评分</view>
      <view class="section-sub">先给这次用餐一个总分，再补一句真实感受。</view>
      <star-rating :value="overallRating" :size="48" @change="onOverallChange" />
      <textarea class="review-textarea" placeholder="写下你的用餐感受..." :value="content" @input="onContentInput" maxlength="500" />
      <view class="textarea-count">{{ content.length }}/500</view>
    </view>

    <view class="section-card" v-if="itemRatings.length > 0">
      <view class="section-label">单品评分</view>
      <view class="section-sub">如果某道菜特别好吃，或者需要改进，可以在这里单独打分。</view>
      <view class="item-rating" v-for="item in itemRatings" :key="item.orderItemId">
        <view class="ir-copy">
          <view class="ir-name">{{ item.dishName }}</view>
          <view class="ir-desc">默认 5 分，可按实际口味调整</view>
        </view>
        <star-rating :value="item.rating" :size="36" @change="onItemChange(item.orderItemId, $event)" />
      </view>
    </view>

    <view class="submit-area">
      <button class="btn-primary submit-btn" @tap="submit">提交评价</button>
    </view>
  </view>
</template>

<script setup>
// 评价下单菜品页
import { reactive, toRefs } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getOrderReview, submitReview } from '@/api/review';
import { getOrder } from '@/api/order';
import { KEYS, get, set } from '@/utils/storage';

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
  const ids = Array.isArray(list) ? list.map((v) => normalizeId(v)) : [];
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

const state = reactive({
  orderId: '',
  order: null,
  overallRating: 5,
  content: '',
  itemRatings: []
});

const { orderId, order, overallRating, content, itemRatings } = toRefs(state);

onLoad((query) => {
  const id = normalizeId(query.orderId || get(KEYS.ORDER_ID));
  state.orderId = id;
  if (!id) {
    uni.showToast({ title: '未找到订单号', icon: 'none' });
    return;
  }
  loadOrder(id);
});

async function loadOrder(id) {
  try {
    const existed = await getOrderReview(id);
    if (existed && existed.id) {
      uni.showToast({ title: '该订单已评价', icon: 'none' });
      setTimeout(() => uni.navigateBack({ delta: 1 }), 400);
      return;
    }

    const order = await getOrder(id);
    if (!canReview(order, id)) {
      uni.showToast({ title: '请先完成支付再评价', icon: 'none' });
      setTimeout(() => uni.navigateBack({ delta: 1 }), 400);
      return;
    }

    const itemRatings = (order.items || []).map((it) => ({
      orderItemId: pickItemId(it),
      rating: 5,
      dishName: it.dishName
    }));
    Object.assign(state, { order, itemRatings });
  } catch (err) {
    uni.showToast({ title: err.message || '获取订单失败', icon: 'none' });
  }
}

function onOverallChange(e) {
  state.overallRating = e.value;
}

function onItemChange(orderItemId, e) {
  const id = normalizeId(orderItemId);
  const rating = e.value;
  state.itemRatings = state.itemRatings.map((it) =>
    it.orderItemId === id ? { ...it, rating } : it
  );
}

function onContentInput(e) {
  state.content = e.detail.value;
}

async function submit() {
  if (!state.orderId) {
    uni.showToast({ title: '订单号无效', icon: 'none' });
    return;
  }

  if (!canReview(state.order, state.orderId)) {
    uni.showToast({ title: '请先完成支付再评价', icon: 'none' });
    return;
  }

  const payload = {
    orderId: state.orderId,
    overallRating: state.overallRating,
    content: state.content,
    itemRatings: state.itemRatings
      .filter((it) => !!it.orderItemId)
      .map((it) => ({ orderItemId: it.orderItemId, rating: it.rating }))
  };

  if (isMockPaidOrder(state.orderId)) {
    await new Promise((resolve) => setTimeout(resolve, 300));
    markOrderReviewed(state.orderId);
    uni.navigateTo({ url: '/pages/result/index?ok=1&type=review' });
    return;
  }

  try {
    const existed = await getOrderReview(state.orderId);
    if (existed && existed.id) {
      uni.showToast({ title: '该订单已评价', icon: 'none' });
      return;
    }
    await submitReview(payload);
    markOrderReviewed(state.orderId);
    uni.navigateTo({ url: '/pages/result/index?ok=1&type=review' });
  } catch (err) {
    uni.showToast({ title: err.message || '提交评价失败', icon: 'none' });
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  position: relative;
  background:
    radial-gradient(circle at top right, rgba(7, 193, 96, 0.10), transparent 24%),
    radial-gradient(circle at bottom left, rgba(236, 219, 191, 0.16), transparent 30%),
    linear-gradient(180deg, #fbf8f4 0%, #f5efe8 58%, #ece3db 100%);
  padding: 18rpx;
  padding-bottom: 144rpx;
  box-sizing: border-box;
}

.page-glow {
  position: absolute;
  border-radius: 999rpx;
  pointer-events: none;
  filter: blur(8rpx);
}

.page-glow-a {
  top: 40rpx;
  right: -88rpx;
  width: 250rpx;
  height: 250rpx;
  background: rgba(7, 193, 96, 0.14);
}

.page-glow-b {
  left: -90rpx;
  bottom: 140rpx;
  width: 300rpx;
  height: 300rpx;
  background: rgba(206, 175, 127, 0.14);
}

.review-header {
  position: relative;
  z-index: 2;
  padding: 22rpx;
  border-radius: 30rpx;
  background:
    linear-gradient(118deg, rgba(255, 255, 255, 0.94) 0%, rgba(255, 249, 239, 0.84) 52%, rgba(226, 247, 232, 0.94) 100%),
    linear-gradient(145deg, #fdf8ef 0%, #eaf8ee 100%);
  border: 1rpx solid rgba(255, 255, 255, 0.82);
  box-shadow: 0 18rpx 42rpx rgba(70, 78, 57, 0.10);
}

.header-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
  color: #057a3d;
  background: rgba(7, 193, 96, 0.10);
  font-size: 21rpx;
  font-weight: 700;
}

.header-title {
  margin-top: 18rpx;
  color: #1f2b1d;
  font-size: 42rpx;
  line-height: 1.18;
  font-weight: 800;
}

.header-desc {
  margin-top: 10rpx;
  color: #788373;
  font-size: 23rpx;
  line-height: 1.6;
}

.section-card {
  margin-top: 16rpx;
  background: rgba(255, 252, 247, 0.96);
  border-radius: 28rpx;
  padding: 22rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 18rpx 40rpx rgba(67, 72, 57, 0.08);
}

.section-label {
  font-size: 30rpx;
  font-weight: 800;
  color: #22301f;
}

.section-sub {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #788373;
  line-height: 1.6;
}

.review-textarea {
  margin-top: 20rpx;
  width: 100%;
  min-height: 220rpx;
  background: rgba(248, 250, 252, 0.76);
  border-radius: 20rpx;
  padding: 18rpx;
  font-size: 26rpx;
  color: #22301f;
  border: 1rpx solid rgba(95, 127, 82, 0.10);
  box-sizing: border-box;
}

.textarea-count {
  text-align: right;
  font-size: 24rpx;
  color: #93a08f;
  margin-top: 10rpx;
}

.item-rating {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 18rpx 0;
}

.item-rating + .item-rating {
  border-top: 1rpx solid rgba(95, 127, 82, 0.10);
}

.ir-copy {
  flex: 1;
  min-width: 0;
}

.ir-name {
  font-size: 26rpx;
  font-weight: 700;
  color: #22301f;
}

.ir-desc {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #788373;
}

.submit-area {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: calc(24rpx + env(safe-area-inset-bottom));
  padding: 10rpx;
  border-radius: 28rpx;
  background: rgba(255, 251, 246, 0.72);
  border: 1rpx solid rgba(255, 255, 255, 0.68);
  box-shadow: 0 24rpx 46rpx rgba(55, 66, 48, 0.14);
  backdrop-filter: blur(18rpx);
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  border-radius: 24rpx;
  font-size: 32rpx;
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  color: #ffffff;
  border: 1rpx solid rgba(7, 193, 96, 0.28);
  box-shadow: 0 12rpx 26rpx rgba(7, 193, 96, 0.18);
}
</style>