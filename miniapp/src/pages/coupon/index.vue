<template>
  <view class="coupon-page">
    <app-navbar title="我的优惠券" />
    <view class="page-glow page-glow-a"></view>
    <view class="page-glow page-glow-b"></view>
    <view class="coupon-hero">
      <view class="hero-chip">我的优惠券</view>
      <view class="hero-title">可用优惠都集中在这里</view>
      <view class="hero-desc">领到的优惠券、已使用和已过期记录都能直接查看。</view>
      <view class="hero-stats">
        <view class="hero-stat">
          <view class="hero-stat-value">{{availableCount}}</view>
          <view class="hero-stat-label">未使用</view>
        </view>
        <view class="hero-stat">
          <view class="hero-stat-value">{{usedCount}}</view>
          <view class="hero-stat-label">已使用</view>
        </view>
        <view class="hero-stat">
          <view class="hero-stat-value">{{expiredCount}}</view>
          <view class="hero-stat-label">已过期</view>
        </view>
      </view>
    </view>

    <view class="tab-row">
      <view
        v-for="t in tabs"
        :key="t.value"
        class="tab-item"
        :class="activeStatus === t.value ? 'is-active' : ''"
        @tap="switchTab(t.value)"
      >
        {{t.label}}
      </view>
    </view>

    <view class="empty" v-if="!loading && coupons.length === 0">
      <view class="empty-icon">券</view>
      <view class="empty-title">暂无优惠券</view>
      <view class="empty-desc">后台发放的优惠券会显示在这里</view>
    </view>

    <view class="list" v-else>
      <view class="coupon-card" :class="'status-' + item.status" v-for="item in coupons" :key="item.id">
        <view class="coupon-main">
          <view class="coupon-name">{{item.couponName}}</view>
          <view class="coupon-rule" v-if="item.couponType === 1">满{{item.thresholdAmount}}减{{item.discountAmount}}</view>
          <view class="coupon-rule" v-else>{{item.discountRate}}折优惠</view>
          <view class="coupon-time">有效期至 {{item.validTo}}</view>
        </view>
        <view class="coupon-side">
          <view class="coupon-value" v-if="item.couponType === 1">¥{{item.discountAmount}}</view>
          <view class="coupon-value" v-else>{{item.discountRate}}折</view>
          <view class="coupon-status">
            {{item.status === 0 ? '未使用' : item.status === 1 ? '已使用' : item.status === 2 ? '已过期' : '已锁定'}}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
// 我的优惠券
import { reactive, toRefs } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getMyCoupons } from '@/api/coupon';

const state = reactive({
  activeStatus: '',
  tabs: [
    { label: '全部', value: '' },
    { label: '未使用', value: 0 },
    { label: '已使用', value: 1 },
    { label: '已过期', value: 2 }
  ],
  loading: false,
  coupons: [],
  availableCount: 0,
  usedCount: 0,
  expiredCount: 0
});

const {
  activeStatus, tabs, loading, coupons, availableCount, usedCount, expiredCount
} = toRefs(state);

onShow(() => {
  loadCoupons();
});

async function loadCoupons() {
  state.loading = true;
  try {
    const params = { pageNum: 1, pageSize: 100 };
    if (state.activeStatus !== '') {
      params.status = state.activeStatus;
    }
    const result = await getMyCoupons(params);
    const list = result.list || [];
    const available = list.filter(item => Number(item.status) === 0).length;
    const used = list.filter(item => Number(item.status) === 1).length;
    const expired = list.filter(item => Number(item.status) === 2).length;
    state.coupons = list;
    state.availableCount = available;
    state.usedCount = used;
    state.expiredCount = expired;
  } catch (err) {
    uni.showToast({ title: err.message || '加载优惠券失败', icon: 'none' });
  } finally {
    state.loading = false;
  }
}

function switchTab(value) {
  state.activeStatus = value;
  loadCoupons();
}
</script>

<style scoped>
.coupon-page {
  min-height: 100vh;
  position: relative;
  padding: 18rpx;
  background:
    radial-gradient(circle at top right, rgba(7, 193, 96, 0.12), transparent 24%),
    radial-gradient(circle at bottom left, rgba(236, 219, 191, 0.22), transparent 30%),
    linear-gradient(180deg, #f8f5ee 0%, #f2eee5 48%, #ece7dc 100%);
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
  bottom: 160rpx;
  width: 300rpx;
  height: 300rpx;
  background: rgba(223, 201, 165, 0.18);
}

.coupon-hero {
  position: relative;
  z-index: 2;
  padding: 22rpx;
  border-radius: 30rpx;
  background: linear-gradient(145deg, #057a3d 0%, #07c160 55%, #18d071 100%);
  box-shadow: 0 24rpx 50rpx rgba(5, 122, 61, 0.20);
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
  color: #f4faef;
  background: rgba(255, 255, 255, 0.14);
  font-size: 21rpx;
  font-weight: 700;
}

.hero-title {
  margin-top: 18rpx;
  color: #fbfdf8;
  font-size: 42rpx;
  line-height: 1.18;
  font-weight: 800;
}

.hero-desc {
  margin-top: 10rpx;
  color: rgba(248, 252, 244, 0.78);
  font-size: 23rpx;
  line-height: 1.6;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12rpx;
  margin-top: 22rpx;
}

.hero-stat {
  padding: 18rpx 16rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.12);
  text-align: center;
}

.hero-stat-value {
  color: #fbfdf8;
  font-size: 30rpx;
  font-weight: 800;
}

.hero-stat-label {
  margin-top: 8rpx;
  color: rgba(248, 252, 244, 0.70);
  font-size: 20rpx;
}

.tab-row {
  display: flex;
  gap: 12rpx;
  margin: 16rpx 0 18rpx;
  flex-wrap: wrap;
}

.tab-item {
  padding: 14rpx 24rpx;
  border-radius: 999rpx;
  background: rgba(255, 252, 247, 0.92);
  color: #7f6e69;
  font-size: 24rpx;
  font-weight: 700;
  box-shadow: 0 12rpx 22rpx rgba(5, 122, 61, 0.06);
}

.tab-item.is-active {
  background: linear-gradient(135deg, #07c160 0%, #057a3d 100%);
  color: #fff;
}

.empty {
  margin-top: 100rpx;
  padding: 54rpx 30rpx;
  border-radius: 30rpx;
  background: rgba(255, 252, 247, 0.96);
  text-align: center;
  color: #7a8088;
  box-shadow: 0 18rpx 42rpx rgba(24, 39, 29, 0.08);
}

.empty-icon {
  width: 88rpx;
  height: 88rpx;
  margin: 0 auto;
  border-radius: 28rpx;
  background: #e8f8ee;
  color: #07c160;
  font-size: 32rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-title {
  margin-top: 18rpx;
  color: #231516;
  font-size: 34rpx;
  font-weight: 800;
}

.empty-desc {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #8f7f73;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.coupon-card {
  display: flex;
  justify-content: space-between;
  padding: 28rpx 26rpx;
  border-radius: 28rpx;
  background: rgba(255, 252, 247, 0.96);
  box-shadow: 0 18rpx 42rpx rgba(24, 39, 29, 0.08);
}

.coupon-card.status-1,
.coupon-card.status-2,
.coupon-card.status-3 {
  opacity: 0.72;
}

.coupon-main {
  flex: 1;
  min-width: 0;
}

.coupon-name {
  color: #231516;
  font-size: 32rpx;
  font-weight: 800;
}

.coupon-rule {
  margin-top: 10rpx;
  color: #07c160;
  font-size: 26rpx;
}

.coupon-time {
  margin-top: 16rpx;
  color: #7a8088;
  font-size: 22rpx;
}

.coupon-side {
  min-width: 130rpx;
  margin-left: 18rpx;
  text-align: right;
}

.coupon-value {
  color: #07c160;
  font-size: 36rpx;
  font-weight: 800;
}

.coupon-status {
  margin-top: 14rpx;
  color: #6b7280;
  font-size: 22rpx;
}
</style>