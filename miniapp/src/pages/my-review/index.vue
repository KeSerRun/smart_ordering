<template>
  <view class="page">
    <app-navbar title="我的评价" />
    <!-- 头部 -->
    <view class="review-header">
      <view class="header-title">我的评价</view>
      <view class="header-desc">共 {{ total }} 条评价</view>
    </view>

    <!-- 评价列表 -->
    <view class="review-list" v-if="list.length > 0">
      <view class="review-card" v-for="review in list" :key="review.id">
        <view class="card-head">
          <text class="card-order-no">订单 #{{ review.orderNo || review.orderId }}</text>
          <text class="card-time">{{ review.createTime || '-' }}</text>
        </view>

        <view class="card-rating">
          <text class="card-label">整体评分</text>
          <star-rating :value="review.overallRating" :size="28" :readonly="true" />
        </view>

        <view class="card-content" v-if="review.content">
          <view class="card-label">评价内容</view>
          <view class="content-text">{{ review.content }}</view>
        </view>

        <!-- 单品评分 -->
        <view class="card-items" v-if="review.itemReviewDisplay.length > 0">
          <view class="card-label">单品评分</view>
          <view class="item-row" v-for="item in review.itemReviewDisplay" :key="item.orderItemId || item.itemId">
            <image
              v-if="item.dishImageUrl && !item.imageError"
              class="item-thumb"
              :src="item.dishImageUrl"
              mode="aspectFill"
              lazy-load
              @error="onItemImageError(review, item)"
            />
            <view v-else class="item-thumb item-thumb-empty">无图</view>
            <text class="item-name">{{ item.displayName }}</text>
            <star-rating :value="item.ratingValue" :size="24" :readonly="true" />
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <empty-state v-else-if="!loading" text="暂无评价" icon="/static/tabbar/profile.png" />

    <!-- 底部提示 -->
    <view class="bottom-tip" v-if="loading">加载中...</view>
    <view class="bottom-tip" v-if="finished && list.length > 0">— 没有更多了 —</view>
  </view>
</template>

<script setup>
// 我的评价页
import { reactive, toRefs } from 'vue';
import { onShow, onReachBottom } from '@dcloudio/uni-app';
import { getMyReviews } from '@/api/review';
import { getOrder } from '@/api/order';
import { env } from '@/config/env';

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
      const order = await getOrder(orderId);
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

const state = reactive({
  loading: false,
  finished: false,
  pageNum: 1,
  pageSize: 20,
  total: 0,
  list: [],
  stars: [1, 2, 3, 4, 5]
});

const { loading, finished, pageNum, pageSize, total, list, stars } = toRefs(state);

function onItemImageError(review, item) {
  const reviewId = String(review && review.id || '');
  const itemId = String(item && (item.itemId || item.orderItemId) || '');
  if (!reviewId || !itemId) return;

  state.list = (state.list || []).map((r) => {
    if (String(r.id) !== reviewId) return r;
    const itemReviewDisplay = (r.itemReviewDisplay || []).map((i) =>
      String(i.itemId || i.orderItemId || '') === itemId ? { ...i, imageError: true } : i
    );
    return { ...r, itemReviewDisplay };
  });
}

onShow(() => {
  reload();
});

onReachBottom(() => {
  loadMore();
});

async function reload() {
  Object.assign(state, {
    loading: false,
    finished: false,
    pageNum: 1,
    total: 0,
    list: []
  });
  await loadMore();
}

async function loadMore() {
  if (state.loading || state.finished) return;

  state.loading = true;
  try {
    const result = await getMyReviews(state.pageNum, state.pageSize);
    const rows = await enrichRows(normalizeRows((result && result.list) || []));
    const total = Number((result && result.total) || 0);
    const next = state.list.concat(rows);
    const finished = next.length >= total || rows.length < state.pageSize;

    Object.assign(state, {
      list: next,
      total,
      pageNum: state.pageNum + 1,
      finished
    });
  } catch (err) {
    uni.showToast({ title: err.message || '加载失败', icon: 'none' });
  } finally {
    state.loading = false;
  }
}
</script>

<style scoped>
/* ========== 页面 ========== */
.page {
  min-height: 100vh;
  background: var(--bg);
  padding-bottom: 40rpx;
}

/* ========== 头部 ========== */
.review-header {
  padding: 24rpx 32rpx 16rpx;
  background: #fff;
}
.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: var(--text-primary);
}
.header-desc {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
}

/* ========== 评价卡片 ========== */
.review-list {
  padding: 16rpx 24rpx;
}
.review-card {
  background: var(--card);
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-order-no {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-primary);
}
.card-time {
  font-size: 24rpx;
  color: var(--text-muted);
}
.card-rating {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
}
.card-label {
  font-size: 24rpx;
  color: var(--text-secondary);
  margin-right: 16rpx;
  flex-shrink: 0;
}
.card-content {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid var(--border);
}
.content-text {
  margin-top: 10rpx;
  font-size: 26rpx;
  color: var(--text-primary);
  line-height: 1.6;
  background: var(--bg);
  border-radius: 10rpx;
  padding: 16rpx;
}

/* 单品评分 */
.card-items {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid var(--border);
}
.item-row {
  display: flex;
  align-items: center;
  margin-top: 10rpx;
  background: var(--bg);
  border-radius: 10rpx;
  padding: 12rpx 16rpx;
  gap: 12rpx;
}
.item-thumb {
  width: 56rpx;
  height: 56rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
  background: #E8E8E8;
}
.item-thumb-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  color: var(--text-muted);
}
.item-name {
  flex: 1;
  font-size: 24rpx;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 底部 */
.bottom-tip {
  text-align: center;
  font-size: 24rpx;
  color: var(--text-muted);
  padding: 20rpx 0;
}
</style>