<template>
  <view class="page">
    <app-navbar title="支付订单" />
    <view class="page-glow page-glow-a"></view>
    <view class="page-glow page-glow-b"></view>

    <view class="payment-hero">
      <view class="hero-chip">确认支付</view>
      <view class="hero-title">确认本单支付信息</view>
      <view class="hero-desc">确认金额后发起微信支付，支付成功后自动返回结果页。</view>
    </view>

    <view class="order-summary" v-if="order">
      <view class="summary-head">
        <text class="summary-no">订单 #{{ order.orderNo }}</text>
        <text class="summary-mode">{{ order.paymentMode === 0 ? '餐前付' : '餐后付' }}</text>
      </view>

      <view class="summary-pills">
        <view class="summary-pill">共 {{ order.items.length }} 道菜</view>
        <view class="summary-pill">状态 {{ order.status === 1 ? '已支付' : '待支付' }}</view>
      </view>

      <view class="summary-items">
        <view class="summary-item" v-for="item in order.items" :key="item.id">
          <text class="si-name">{{ item.dishName }}</text>
          <text class="si-qty">×{{ item.quantity }}</text>
          <text class="si-price">¥{{ item.price }}</text>
        </view>
      </view>

      <view class="summary-amount">
        <text class="amount-label">应付金额</text>
        <view class="amount-value">
          <text class="amount-symbol">¥</text>
          <text class="amount-num">{{ order.actualAmount }}</text>
        </view>
      </view>
    </view>

    <view class="pay-section">
      <view class="section-title">支付方式</view>
      <view class="section-desc">当前仅开放微信支付，支付成功后自动返回结果页。</view>
      <view class="pay-method wechat">
        <text class="method-icon">微</text>
        <text class="method-name">微信支付</text>
        <view class="method-check">✓</view>
      </view>

      <view class="pay-submit">
        <view class="pay-status" v-if="paymentStatusText">{{ paymentStatusText }}</view>
        <button class="btn-primary pay-btn" @tap="payNow" :loading="paying" :disabled="paying">
          {{ paying ? '支付处理中...' : '立即支付' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
// 收银页
import { reactive, toRefs } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import * as orderApi from '@/api/order';
import * as paymentApi from '@/api/payment';
import { KEYS, get, set } from '@/utils/storage';

function normalizeOrderId(v) {
  if (v === null || v === undefined) return '';
  const s = String(v).trim();
  return s && s !== '0' ? s : '';
}

function amountToFen(value) {
  return Math.round(Number(value || 0) * 100);
}

// ===== 响应式状态 =====
const state = reactive({
  orderId: '',
  order: null,
  paying: false,
  paymentStatusText: ''
});

const { orderId, order, paying, paymentStatusText } = toRefs(state);

// ===== 生命周期 =====
onLoad((query) => {
  const id = normalizeOrderId(query.orderId || get(KEYS.ORDER_ID));
  state.orderId = id;
  if (!id) {
    uni.showToast({ title: '未找到订单号，请重新下单', icon: 'none' });
    return;
  }
  set(KEYS.ORDER_ID, id);
  loadOrder(id);
});

// ===== 方法 =====
async function loadOrder(id) {
  try {
    const order = await orderApi.getOrder(id);
    state.order = order;
  } catch (err) {
    uni.showToast({ title: err.message || '获取订单失败', icon: 'none' });
  }
}

async function payNow() {
  if (state.paying) return;

  const id = normalizeOrderId(state.orderId || get(KEYS.ORDER_ID));
  if (!id) {
    uni.showToast({ title: '订单号丢失，请返回重试', icon: 'none' });
    return;
  }

  const amount = Number((state.order && state.order.actualAmount) || 0);
  if (!(amount > 0)) {
    uni.showToast({ title: '订单金额无效', icon: 'none' });
    return;
  }

  if (state.order && Number(state.order.status) === 1) {
    uni.showToast({ title: '该订单已支付', icon: 'none' });
    uni.navigateTo({ url: `/pages/result/index?ok=1&orderId=${id}` });
    return;
  }

  state.paying = true;
  state.paymentStatusText = '支付处理中...';

  try {
    const payData = await paymentApi.wechatPay(id);
    // 开发环境 mock 支付：后端已模拟支付成功，直接跳成功页，不拉起微信支付
    if (payData && payData.mockPaid) {
      await loadOrder(id);
      state.paymentStatusText = '支付成功';
      uni.navigateTo({ url: `/pages/result/index?ok=1&orderId=${id}` });
      return;
    }
    if (payData && payData.amount !== undefined && amountToFen(payData.amount) !== amountToFen(amount)) {
      await loadOrder(id);
      throw new Error('支付金额已更新，请重新确认后再支付');
    }
    await requestWechatPayment(payData);
    await loadOrder(id);
    state.paymentStatusText = '支付成功';
    uni.navigateTo({ url: `/pages/result/index?ok=1&orderId=${id}` });
  } catch (err) {
    uni.showToast({ title: err.message || '支付失败', icon: 'none' });
  } finally {
    state.paying = false;
  }
}

function requestWechatPayment(payData) {
  if (!payData || !payData.timeStamp || !payData.nonceStr || !payData.packageValue || !payData.paySign) {
    return Promise.reject(new Error('后端未返回完整的微信支付参数'));
  }

  return new Promise((resolve, reject) => {
    uni.requestPayment({
      timeStamp: String(payData.timeStamp),
      nonceStr: payData.nonceStr,
      package: payData.packageValue,
      signType: payData.signType || 'RSA',
      paySign: payData.paySign,
      success: resolve,
      fail: (err) => {
        console.error('微信支付拉起失败', {
          errMsg: err && err.errMsg,
          errno: err && err.errno,
          errCode: err && err.errCode,
          raw: err,
          payParams: {
            appId: payData.appId,
            timeStamp: String(payData.timeStamp),
            nonceStr: payData.nonceStr,
            packageValue: payData.packageValue,
            signType: payData.signType || 'RSA',
            paySignLength: payData.paySign ? String(payData.paySign).length : 0
          }
        });
        if (err && err.errMsg && err.errMsg.includes('cancel')) {
          reject(new Error('已取消支付'));
          return;
        }
        reject(new Error((err && err.errMsg) || '微信支付失败'));
      }
    });
  });
}

function goReview() {
  uni.navigateTo({ url: `/pages/review/index?orderId=${state.orderId}` });
}
</script>

<style scoped>
.page {
  --wechat-green: #07c160;
  --wechat-green-deep: #06ad56;
  --pay-amount: #b46a12;
  min-height: 100vh;
  position: relative;
  background:
    radial-gradient(circle at top right, rgba(7, 193, 96, 0.10), transparent 24%),
    radial-gradient(circle at bottom left, rgba(236, 219, 191, 0.18), transparent 30%),
    linear-gradient(180deg, #fbf8f4 0%, #f2ede6 48%, #ece3db 100%);
  padding: 18rpx 18rpx calc(40rpx + env(safe-area-inset-bottom));
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

.payment-hero {
  position: relative;
  z-index: 2;
  padding: 22rpx;
  border-radius: 30rpx;
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.18), transparent 30%),
    linear-gradient(145deg, #057a3d 0%, #07c160 55%, #18d071 100%);
  box-shadow: 0 24rpx 50rpx rgba(7, 193, 96, 0.20);
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

.order-summary,
.pay-section {
  margin-top: 16rpx;
  background: rgba(255, 252, 247, 0.96);
  border-radius: 28rpx;
  padding: 22rpx;
  box-shadow: 0 18rpx 40rpx rgba(67, 72, 57, 0.08);
}

.summary-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.summary-no {
  font-size: 30rpx;
  font-weight: 800;
  color: #22301f;
}

.summary-mode,
.summary-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42rpx;
  padding: 0 14rpx;
  border-radius: 999rpx;
  font-size: 21rpx;
  font-weight: 700;
}

.summary-mode {
  color: #07a857;
  background: #e8f8ee;
}

.summary-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 16rpx;
}

.summary-pill {
  color: #62715d;
  background: #f2f5ec;
}

.summary-items {
  margin-top: 16rpx;
  border-top: 1rpx solid rgba(95, 127, 82, 0.10);
  padding-top: 12rpx;
}

.summary-item {
  display: flex;
  align-items: center;
  padding: 12rpx 0;
  font-size: 26rpx;
}

.summary-item + .summary-item {
  border-top: 1rpx solid rgba(95, 127, 82, 0.08);
}

.si-name {
  flex: 1;
  color: #22301f;
}

.si-qty {
  color: #768271;
  margin-right: 16rpx;
}

.si-price {
  color: #22301f;
  font-weight: 700;
}

.summary-amount {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid rgba(95, 127, 82, 0.10);
}

.amount-label {
  font-size: 26rpx;
  color: #768271;
}

.amount-value {
  color: var(--pay-amount);
  font-weight: 800;
}

.amount-symbol {
  font-size: 28rpx;
}

.amount-num {
  font-size: 44rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #22301f;
}

.section-desc {
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #788373;
}

.pay-method {
  display: flex;
  align-items: center;
  padding: 20rpx;
  border-radius: 22rpx;
  background: #f5f6f1;
  margin-top: 18rpx;
  margin-bottom: 24rpx;
}

.pay-method.wechat {
  border: 1rpx solid rgba(7, 193, 96, 0.14);
  background: rgba(7, 193, 96, 0.08);
}

.pay-submit {
  position: relative;
  z-index: 2;
  width: 100%;
  margin-top: 0;
  padding: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.method-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  color: #f7fcf3;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 800;
  margin-right: 12rpx;
}

.method-name {
  flex: 1;
  font-size: 28rpx;
  font-weight: 700;
  color: #22301f;
}

.method-check {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: var(--wechat-green);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

.pay-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin: 0;
  padding: 0;
  height: 96rpx;
  border-radius: 22rpx;
  font-size: 32rpx;
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  border: 1rpx solid rgba(7, 193, 96, 0.24);
  color: #ffffff;
  box-sizing: border-box;
  box-shadow: 0 12rpx 26rpx rgba(7, 193, 96, 0.22);
}

.pay-btn[disabled] {
  background: #eef4ef !important;
  border-color: rgba(145, 173, 154, 0.4) !important;
  color: #91ad9a !important;
  box-shadow: none !important;
}

.pay-status {
  margin-bottom: 14rpx;
  text-align: center;
  font-size: 24rpx;
  color: #788373;
}
</style>