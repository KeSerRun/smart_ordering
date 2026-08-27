<template>
  <view class="result-page">
    <view class="page-glow page-glow-a"></view>
    <view class="page-glow page-glow-b"></view>
    <view class="result-card">
      <view class="result-icon" :class="ok ? 'success' : 'fail'">
        <text>{{ok ? '✓' : '✕'}}</text>
      </view>

      <view class="result-title">{{resultTitle}}</view>
      <view class="result-sub">{{resultSub}}</view>

      <view class="result-desc" v-if="resultDesc">{{resultDesc}}</view>

      <view class="reward-card" v-if="ok && type !== 'review' && orderId">
        <view class="reward-card__title">本次会员到账</view>
        <view class="reward-card__loading" v-if="rewardLoading">正在同步会员奖励...</view>
        <block v-else-if="rewardSummary">
          <view class="reward-grid">
            <view class="reward-metric">
              <text class="reward-value">+{{rewardSummary.pointsReward || 0}}</text>
              <text class="reward-label">积分</text>
            </view>
            <view class="reward-metric">
              <text class="reward-value">+{{rewardSummary.growthReward || 0}}</text>
              <text class="reward-label">成长值</text>
            </view>
          </view>
          <view class="reward-note" v-if="rewardSummary.settled">本次支付奖励已入账，可前往会员中心查看明细。</view>
          <view class="reward-note reward-note--muted" v-else>会员奖励正在处理或本单未产生奖励，可稍后到会员中心查看。</view>
        </block>
      </view>

      <view class="result-actions">
        <button class="btn-outline" @tap="backHome">返回首页</button>
        <button class="btn-primary" @tap="goOrder">查看订单</button>
      </view>
    </view>
  </view>
</template>

<script setup>
// 操作结果页
import { reactive, toRefs } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getMemberRewardSummary } from '@/api/member';

function buildResultCopy(ok, type) {
  if (type === 'review') {
    return ok
      ? { title: '反馈已提交', sub: '感谢评价', desc: '感谢你的反馈，欢迎再次光临！' }
      : { title: '提交失败', sub: '反馈未保存', desc: '请返回后重新提交评价。' };
  }

  return ok
    ? { title: '支付完成', sub: '订单已更新', desc: '' }
    : { title: '支付未完成', sub: '订单仍待处理', desc: '如已扣款，请稍后在订单页查看支付状态。' };
}

const state = reactive({
  ok: true,
  type: 'payment',
  orderId: null,
  resultTitle: '支付完成',
  resultSub: '订单已更新',
  resultDesc: '',
  rewardSummary: null,
  rewardLoading: false
});

const {
  ok, type, orderId, resultTitle, resultSub, resultDesc, rewardSummary, rewardLoading
} = toRefs(state);

onLoad((query) => {
  const ok = query.ok === '1';
  const type = query.type || 'payment';
  const copy = buildResultCopy(ok, type);
  state.ok = ok;
  state.type = type;
  state.orderId = query.orderId ? Number(query.orderId) : null;
  state.resultTitle = copy.title;
  state.resultSub = copy.sub;
  state.resultDesc = copy.desc;
});

onShow(() => {
  loadRewardSummary();
});

async function loadRewardSummary() {
  if (!state.ok || state.type === 'review' || !state.orderId) {
    return;
  }
  state.rewardLoading = true;
  try {
    const rewardSummary = await getMemberRewardSummary(state.orderId);
    state.rewardSummary = rewardSummary || null;
  } catch (err) {
    state.rewardSummary = null;
  } finally {
    state.rewardLoading = false;
  }
}

function backHome() {
  uni.reLaunch({ url: '/pages/index/index' });
}

function goOrder() {
  uni.switchTab({
    url: '/pages/order/index',
    fail: () => {
      uni.reLaunch({ url: '/pages/order/index' });
    }
  });
}
</script>

<style scoped>
.result-page {
  --result-red: #07c160;
  --result-red-deep: #057a3d;
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at top right, rgba(7, 193, 96, 0.10), transparent 24%),
    radial-gradient(circle at bottom left, rgba(236, 219, 191, 0.16), transparent 30%),
    linear-gradient(180deg, #fbf8f4 0%, #f2ede6 48%, #ece3db 100%);
  padding: 28rpx;
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
  bottom: 100rpx;
  width: 300rpx;
  height: 300rpx;
  background: rgba(206, 175, 127, 0.14);
}

.result-card {
  position: relative;
  z-index: 2;
  text-align: center;
  width: 100%;
  max-width: 680rpx;
  padding: 40rpx 30rpx 30rpx;
  border-radius: 34rpx;
  background: rgba(255, 252, 247, 0.96);
  box-shadow: 0 24rpx 52rpx rgba(67, 72, 57, 0.10);
}

.result-icon {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24rpx;
  font-size: 72rpx;
  font-weight: 700;
}

.result-icon.success {
  background: #e8f8ee;
  color: var(--result-red);
}

.result-icon.fail {
  background: #fff1f0;
  color: #ff4d4f;
}

.result-title {
  font-size: 42rpx;
  font-weight: 800;
  color: #22301f;
}

.result-sub {
  margin-top: 10rpx;
  color: var(--result-red);
  font-size: 24rpx;
  font-weight: 700;
}

.result-desc {
  font-size: 26rpx;
  line-height: 1.7;
  color: #788373;
  margin: 18rpx 0 0;
}

.member-tip {
  margin-top: 12rpx;
  color: var(--result-red);
  font-size: 24rpx;
}

.reward-card {
  margin: 28rpx auto 32rpx;
  padding: 28rpx 30rpx;
  border-radius: 28rpx;
  background: linear-gradient(180deg, #f8fbf4 0%, #ffffff 100%);
  box-shadow: 0 18rpx 44rpx rgba(76, 105, 67, 0.08);
  text-align: left;
}

.reward-card__title {
  font-size: 28rpx;
  font-weight: 800;
  color: #22301f;
}

.reward-card__loading {
  margin-top: 18rpx;
  font-size: 24rpx;
  color: #788373;
}

.reward-grid {
  display: flex;
  gap: 20rpx;
  margin-top: 22rpx;
}

.reward-metric {
  flex: 1;
  padding: 24rpx 20rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: inset 0 0 0 2rpx rgba(95, 127, 82, 0.08);
}

.reward-value {
  display: block;
  font-size: 42rpx;
  font-weight: 800;
  color: var(--result-red);
}

.reward-label {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #788373;
}

.reward-note {
  margin-top: 20rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: var(--result-red);
}

.reward-note--muted {
  color: #788373;
}

.result-actions {
  display: flex;
  gap: 20rpx;
  justify-content: center;
}

.result-actions button {
  width: 240rpx;
  height: 80rpx;
  font-size: 28rpx;
  font-weight: 700;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-outline {
  background: #fff;
  color: var(--result-red);
  border: 2rpx solid var(--result-red);
}

.btn-member {
  width: 360rpx;
  height: 76rpx;
  margin: 24rpx auto 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24rpx;
  background: #e8f8ee;
  color: var(--result-red);
  font-size: 26rpx;
  font-weight: 700;
}
</style>