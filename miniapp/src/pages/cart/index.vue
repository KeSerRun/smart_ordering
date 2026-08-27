<template>
  <view class="page">
    <app-navbar title="购物车" backgroundColor="#fbf8f4" />
    <view class="page-glow page-glow-a"></view>
    <view class="page-glow page-glow-b"></view>

    <view class="cart-header">
      <view class="cart-header-top">
        <view>
          <view class="header-chip">购物车</view>
          <view class="header-title">确认已选菜品</view>
          <view class="header-desc">先确认菜品数量、口味备注和优惠方式，再提交订单。</view>
        </view>
        <view class="header-count">{{ cart.totalCount || 0 }} 件</view>
      </view>

      <view class="header-meta-row">
        <view class="header-meta-pill" v-if="tableId">桌号 {{ tableId }}</view>
        <view class="header-meta-pill">菜品 {{ cart.items.length || 0 }} 道</view>
        <view class="header-meta-pill">应付 ¥{{ payableTotal }}</view>
      </view>
    </view>

    <view class="summary-card" v-if="cart.items && cart.items.length > 0">
      <view class="summary-head">
        <view>
          <view class="summary-title">本单摘要</view>
          <view class="summary-desc">可先调整数量、备注和优惠方式。</view>
        </view>
        <view class="summary-clear" @tap="clearCart">清空</view>
      </view>
      <view class="summary-pills">
        <view class="summary-pill">原价 ¥{{ cart.totalPrice }}</view>
        <view class="summary-pill">{{ selectedCouponText }}</view>
        <view class="summary-pill">{{ pointsText }}</view>
      </view>
    </view>

    <view class="cart-list" v-if="cart.items && cart.items.length > 0">
      <view class="cart-item" v-for="item in cart.items" :key="item.dishId">
        <view class="item-main">
          <view class="item-info">
            <view class="item-name-row">
              <view class="item-name">{{ item.dishName }}</view>
              <view class="item-badge">x{{ item.quantity }}</view>
            </view>
            <view class="item-price">¥{{ item.price }}</view>
          </view>
          <view class="item-subtotal">¥{{ item.amount || item.subtotal }}</view>
        </view>

        <view class="item-ops">
          <view class="qty-stepper">
            <view class="qty-btn" @tap="decrease(item)">−</view>
            <text class="qty-num">{{ item.quantity }}</text>
            <view class="qty-btn" @tap="increase(item)">+</view>
          </view>

          <view class="remark-wrap">
            <input class="remark-input" :value="item.remark" placeholder="口味备注" @input="onRemarkInput(item.dishId, $event)" />
            <text class="remark-save" @tap="saveRemark(item.dishId)">保存</text>
          </view>
        </view>
      </view>
    </view>

    <empty-state v-else text="购物车是空的" icon="/static/tabbar/cart.png" actionText="去点餐" actionClass="cart-empty-action" @action="goMenu" />

    <view class="bottom-bar safe-bottom" v-if="cart.items && cart.items.length > 0">
      <view class="bar-info">
        <view class="coupon-row" @tap="choosePoints" v-if="benefitOverview && benefitOverview.pointsDeductionRule && benefitOverview.pointsDeductionRule.enabled">
          <text class="coupon-row-label">积分抵现</text>
          <text class="coupon-row-value">{{ pointsText }}</text>
        </view>
        <view class="coupon-row" @tap="chooseCoupon">
          <text class="coupon-row-label">优惠券</text>
          <text class="coupon-row-value">{{ selectedCouponText }}</text>
        </view>
        <view class="bar-total-wrap">
          <view class="bar-total-label">合计</view>
          <view class="bar-total-price">¥{{ payableTotal }}</view>
        </view>
      </view>
      <button class="btn-primary bar-submit" @tap="submitOrder">提交订单</button>
    </view>
  </view>
</template>

<script setup>
// 购物车页
import { reactive, toRefs } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import * as cartApi from '@/api/cart';
import * as couponApi from '@/api/coupon';
import * as memberApi from '@/api/member';
import * as orderApi from '@/api/order';
import { formatPrice } from '@/utils/format';
import { KEYS, get, set, getTableBindingKey } from '@/utils/storage';

const WEEKDAY_LABELS = {
  1: '周一',
  2: '周二',
  3: '周三',
  4: '周四',
  5: '周五',
  6: '周六',
  7: '周日'
};

function getCurrentWeekday() {
  const weekday = new Date().getDay();
  return weekday === 0 ? 7 : weekday;
}

function formatAvailableWeekdays(availableWeekdays) {
  if (!availableWeekdays) return '全周可用';
  const labels = String(availableWeekdays)
    .split(',')
    .map(item => WEEKDAY_LABELS[Number(item)])
    .filter(Boolean);
  return labels.length ? labels.join('、') : '全周可用';
}

function isCouponAvailableToday(coupon) {
  if (!coupon || !coupon.availableWeekdays) return true;
  const currentWeekday = getCurrentWeekday();
  return String(coupon.availableWeekdays)
    .split(',')
    .map(item => Number(item))
    .includes(currentWeekday);
}

function pickOrderId(order) {
  if (!order || typeof order !== 'object') return '';
  const raw = order.id ?? order.orderId ?? '';
  return raw === null || raw === undefined ? '' : String(raw);
}

function pickLatestPendingOrder(orders) {
  if (!Array.isArray(orders) || orders.length === 0) return null;
  return orders.find(order => Number(order.status) === 0) || null;
}

function normalizeDishId(value) {
  if (value === null || value === undefined) return '';
  return String(value);
}

// ===== 响应式状态 =====
const state = reactive({
  tableId: null,
  tableBindingKey: '',
  availableCoupons: [],
  selectedCouponId: null,
  selectedCouponText: '暂不使用优惠券',
  memberCenter: null,
  benefitOverview: null,
  requestedPoints: 0,
  actualUsedPoints: 0,
  pointsDeductionAmount: '0.00',
  pointsText: '暂不使用积分',
  payableTotal: '0.00',
  cart: {
    items: [],
    totalCount: 0,
    totalPrice: '0.00'
  }
});

const {
  tableId, availableCoupons, selectedCouponId, selectedCouponText,
  memberCenter, benefitOverview, requestedPoints, actualUsedPoints,
  pointsDeductionAmount, pointsText, payableTotal, cart
} = toRefs(state);

// ===== 生命周期 =====
onLoad((query) => {
  const table = get(KEYS.TABLE) || {};
  const tableId = Number(query.tableId || table.id);
  state.tableId = tableId;
  state.tableBindingKey = getTableBindingKey(table);
});

onShow(() => {
  const table = get(KEYS.TABLE) || {};
  const tableId = Number(table.id || 0);
  const tableBindingKey = getTableBindingKey(table);
  if (tableBindingKey && tableBindingKey !== state.tableBindingKey) {
    state.tableId = tableId;
    state.tableBindingKey = tableBindingKey;
    state.selectedCouponId = null;
    state.selectedCouponText = '暂不使用优惠券';
    state.requestedPoints = 0;
    state.actualUsedPoints = 0;
    state.pointsDeductionAmount = '0.00';
    state.pointsText = '暂不使用积分';
  } else if (tableId && tableId !== Number(state.tableId || 0)) {
    state.tableId = tableId;
    state.tableBindingKey = tableBindingKey;
  }
  loadCart();
  loadCoupons();
  loadMemberBenefits();
});

// ===== 方法 =====
async function loadCart() {
  if (!state.tableId) return;
  try {
    const cartData = await cartApi.getCart(state.tableId);
    state.cart = {
      ...cartData,
      totalPrice: formatPrice(cartData.totalPrice),
      items: cartData.items || []
    };
    syncPointsDeduction();
  } catch (err) {
    state.cart = { items: [], totalCount: 0, totalPrice: '0.00' };
    state.payableTotal = '0.00';
  }
}

async function loadMemberBenefits() {
  try {
    const [memberCenter, benefitOverview] = await Promise.all([
      memberApi.getMemberCenter(),
      memberApi.getMemberBenefitOverview()
    ]);
    state.memberCenter = memberCenter;
    state.benefitOverview = benefitOverview;
    syncPointsDeduction();
  } catch (err) {
    state.memberCenter = null;
    state.benefitOverview = null;
    state.requestedPoints = 0;
    state.actualUsedPoints = 0;
    state.pointsDeductionAmount = '0.00';
  }
}

async function loadCoupons() {
  try {
    const result = await couponApi.getMyCoupons({ status: 0, pageNum: 1, pageSize: 100 });
    state.availableCoupons = result.list || [];
    syncSelectedCoupon();
  } catch (err) {
    state.availableCoupons = [];
  }
}

async function increase(item) {
  await updateQuantity(item, Number(item.quantity || 0) + 1);
}

async function decrease(item) {
  await updateQuantity(item, Number(item.quantity || 0) - 1);
}

async function updateQuantity(item, quantity) {
  try {
    if (quantity <= 0) {
      await cartApi.removeCartItem(item.dishId, state.tableId);
    } else {
      await cartApi.updateCartItem(item.dishId, state.tableId, quantity);
    }
    loadCart();
  } catch (err) {
    uni.showToast({ title: err.message || '更新失败', icon: 'none' });
  }
}

function onRemarkInput(dishId, e) {
  const id = normalizeDishId(dishId);
  const value = e.detail.value;
  state.cart.items = state.cart.items.map((it) => (
    normalizeDishId(it.dishId) === id ? { ...it, remark: value } : it
  ));
}

async function saveRemark(dishId) {
  const id = normalizeDishId(dishId);
  const item = state.cart.items.find(it => normalizeDishId(it.dishId) === id);
  if (!item) {
    uni.showToast({ title: '未找到当前菜品', icon: 'none' });
    return;
  }
  try {
    await cartApi.updateCartItem(item.dishId, state.tableId, undefined, item.remark ?? '');
    await loadCart();
    uni.showToast({ title: '备注已保存', icon: 'none' });
  } catch (err) {
    uni.showToast({ title: err.message || '备注保存失败', icon: 'none' });
  }
}

async function clearCart() {
  try {
    await cartApi.clearCart(state.tableId);
    loadCart();
    uni.showToast({ title: '已清空', icon: 'none' });
  } catch (err) {
    uni.showToast({ title: err.message || '清空失败', icon: 'none' });
  }
}

function goMenu() {
  uni.switchTab({ url: '/pages/menu/index' });
}

function syncSelectedCoupon() {
  const selectedCoupon = state.availableCoupons.find(item => Number(item.id) === Number(state.selectedCouponId || 0));
  if (!selectedCoupon) {
    state.selectedCouponId = null;
    state.selectedCouponText = '暂不使用优惠券';
    return;
  }

  const thresholdAmount = Number(selectedCoupon.thresholdAmount || 0);
  const cartTotal = Number(state.cart.totalPrice || 0);
  const ruleText = selectedCoupon.couponType === 1
    ? `满${selectedCoupon.thresholdAmount}减${selectedCoupon.discountAmount}`
    : `${selectedCoupon.discountRate}折优惠`;

  const weekdayText = formatAvailableWeekdays(selectedCoupon.availableWeekdays);

  if (selectedCoupon.couponType === 1 && cartTotal < thresholdAmount) {
    state.selectedCouponText = `${ruleText}（${weekdayText}，当前金额未达门槛）`;
    return;
  }

  state.selectedCouponText = `${ruleText}（${weekdayText}）`;
}

function syncPointsDeduction() {
  const rule = state.benefitOverview?.pointsDeductionRule;
  const pointsBalance = Number(state.memberCenter?.pointsBalance || 0);
  const cartTotal = Number(state.cart.totalPrice || 0);
  if (!rule || !rule.enabled || cartTotal <= 0 || pointsBalance <= 0) {
    state.requestedPoints = 0;
    state.actualUsedPoints = 0;
    state.pointsDeductionAmount = '0.00';
    state.pointsText = '暂不使用积分';
    state.payableTotal = formatPrice(cartTotal);
    return;
  }
  const pointsPerStep = Number(rule.pointsPerStep || 0);
  const amountPerStep = Number(rule.amountPerStep || 0);
  const maxRatio = Number(rule.maxDeductionRatio || 0);
  const maxPointsPerOrder = Number(rule.maxPointsPerOrder || 0);
  if (pointsPerStep <= 0 || amountPerStep <= 0) {
    state.requestedPoints = 0;
    state.actualUsedPoints = 0;
    state.pointsDeductionAmount = '0.00';
    state.pointsText = '暂不使用积分';
    state.payableTotal = formatPrice(cartTotal);
    return;
  }

  const maxDeductionAmount = Math.floor(cartTotal * maxRatio * 100) / 100;
  const maxByAmount = Math.floor(maxDeductionAmount / amountPerStep) * pointsPerStep;
  let maxPoints = Math.min(pointsBalance, maxByAmount);
  if (maxPointsPerOrder > 0) {
    maxPoints = Math.min(maxPoints, maxPointsPerOrder);
  }
  const normalizedMax = Math.floor(maxPoints / pointsPerStep) * pointsPerStep;
  const requested = Math.min(Number(state.requestedPoints || 0), normalizedMax);
  const actualUsedPoints = Math.floor(requested / pointsPerStep) * pointsPerStep;
  const deductionAmount = ((actualUsedPoints / pointsPerStep) * amountPerStep).toFixed(2);
  const payableTotal = formatPrice(Math.max(cartTotal - Number(deductionAmount || 0), 0));
  state.actualUsedPoints = actualUsedPoints;
  state.pointsDeductionAmount = deductionAmount;
  state.pointsText = actualUsedPoints > 0 ? `${actualUsedPoints} 积分抵 ¥${deductionAmount}` : '暂不使用积分';
  state.payableTotal = payableTotal;
}

function choosePoints() {
  const rule = state.benefitOverview?.pointsDeductionRule;
  const pointsBalance = Number(state.memberCenter?.pointsBalance || 0);
  const cartTotal = Number(state.cart.totalPrice || 0);
  if (!rule || !rule.enabled || cartTotal <= 0 || pointsBalance <= 0) {
    uni.showToast({ title: '当前不可用积分抵现', icon: 'none' });
    return;
  }

  const pointsPerStep = Number(rule.pointsPerStep || 0);
  const amountPerStep = Number(rule.amountPerStep || 0);
  const maxRatio = Number(rule.maxDeductionRatio || 0);
  const maxPointsPerOrder = Number(rule.maxPointsPerOrder || 0);
  const maxDeductionAmount = Math.floor(cartTotal * maxRatio * 100) / 100;
  const maxByAmount = Math.floor(maxDeductionAmount / amountPerStep) * pointsPerStep;
  let maxPoints = Math.min(pointsBalance, maxByAmount);
  if (maxPointsPerOrder > 0) maxPoints = Math.min(maxPoints, maxPointsPerOrder);
  const normalizedMax = Math.floor(maxPoints / pointsPerStep) * pointsPerStep;

  if (normalizedMax <= 0) {
    uni.showToast({ title: '本单暂不可使用积分', icon: 'none' });
    return;
  }

  const options = ['不使用积分'];
  for (let points = pointsPerStep; points <= normalizedMax; points += pointsPerStep) {
    const amount = ((points / pointsPerStep) * amountPerStep).toFixed(2);
    options.push(`${points} 积分抵 ${amount} 元`);
  }

  uni.showActionSheet({
    itemList: options,
    success: res => {
      if (res.tapIndex === 0) {
        state.requestedPoints = 0;
        syncPointsDeduction();
        return;
      }
      const selectedPoints = res.tapIndex * pointsPerStep;
      state.requestedPoints = selectedPoints;
      syncPointsDeduction();
    }
  });
}

function chooseCoupon() {
  const options = [{ label: '不使用优惠券', value: 0 }];
  state.availableCoupons.forEach(item => {
    const ruleLabel = item.couponType === 1
      ? `${item.couponName}｜满${item.thresholdAmount}减${item.discountAmount}`
      : `${item.couponName}｜${item.discountRate}折优惠`;
    const weekdayLabel = formatAvailableWeekdays(item.availableWeekdays);
    const disabledTip = isCouponAvailableToday(item) ? '' : '｜今日不可用';
    options.push({ label: `${ruleLabel}｜${weekdayLabel}${disabledTip}`, value: item.id });
  });

  uni.showActionSheet({
    itemList: options.map(item => item.label),
    success: (res) => {
      const selected = options[res.tapIndex];
      if (!selected || selected.value === 0) {
        state.selectedCouponId = null;
        state.selectedCouponText = '暂不使用优惠券';
        return;
      }
      const selectedCoupon = state.availableCoupons.find(item => Number(item.id) === Number(selected.value));
      if (selectedCoupon && !isCouponAvailableToday(selectedCoupon)) {
        uni.showToast({ title: `${formatAvailableWeekdays(selectedCoupon.availableWeekdays)}可用`, icon: 'none' });
        return;
      }
      state.selectedCouponId = selected.value;
      syncSelectedCoupon();
    }
  });
}

async function submitOrder() {
  if (!state.cart.items.length) {
    uni.showToast({ title: '购物车为空', icon: 'none' });
    return;
  }

  try {
    const tableOrders = await orderApi.getTableOrders(state.tableId);
    const pendingOrder = pickLatestPendingOrder(tableOrders);

    if (pendingOrder) {
      if (state.selectedCouponId || Number(state.actualUsedPoints || 0) > 0) {
        uni.showToast({ title: '当前为加菜并单，暂不支持重新选择优惠券或积分', icon: 'none' });
        return;
      }

      const pendingOrderId = pickOrderId(pendingOrder);
      if (!pendingOrderId) {
        uni.showToast({ title: '当前活动订单异常，请稍后重试', icon: 'none' });
        return;
      }

      for (const item of state.cart.items) {
        await orderApi.addOrderItem(pendingOrderId, {
          dishId: item.dishId,
          quantity: item.quantity,
          remark: item.remark || ''
        });
      }

      await cartApi.clearCart(state.tableId);
      uni.showToast({ title: '加菜成功', icon: 'none' });
      uni.switchTab({ url: '/pages/order/index' });
      return;
    }

    const order = await orderApi.createOrder({
      tableId: state.tableId,
      paymentMode: 0,
      orderType: 0,
      remark: '',
      couponId: state.selectedCouponId || undefined,
      usePoints: state.actualUsedPoints || undefined
    });

    const orderId = pickOrderId(order);
    if (!orderId) {
      uni.showToast({ title: '下单成功但订单号缺失', icon: 'none' });
      return;
    }

    set(KEYS.ORDER_ID, orderId);
    uni.showToast({ title: '下单成功', icon: 'none' });
    uni.navigateTo({ url: `/pages/payment/index?orderId=${orderId}` });
  } catch (err) {
    uni.showToast({ title: err.message || '下单失败', icon: 'none' });
  }
}
</script>

<style scoped>
.page {
  --cart-red: #07c160;
  --cart-red-deep: #057a3d;
  --cart-subtext: #8a7974;

  min-height: 100vh;
  position: relative;
  background:
    radial-gradient(circle at top right, rgba(7, 193, 96, 0.10), transparent 24%),
    radial-gradient(circle at bottom left, rgba(236, 219, 191, 0.18), transparent 30%),
    linear-gradient(180deg, #fbf8f4 0%, #f2ede6 48%, #ece3db 100%);
  padding-bottom: 186rpx;
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
  background: rgba(206, 175, 127, 0.14);
}

.cart-header {
  position: relative;
  z-index: 2;
  margin: 18rpx 18rpx 0;
  padding: 22rpx;
  border-radius: 30rpx;
  background: linear-gradient(145deg, #057a3d 0%, #07c160 55%, #18d071 100%);
  box-shadow: 0 24rpx 50rpx rgba(5, 122, 61, 0.2);
}

.cart-header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16rpx;
}

.header-chip,
.header-count,
.header-meta-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
  font-size: 21rpx;
  font-weight: 700;
}

.header-chip {
  color: #f4faef;
  background: rgba(255, 255, 255, 0.14);
}

.header-title {
  margin-top: 18rpx;
  color: #fbfdf8;
  font-size: 42rpx;
  line-height: 1.18;
  font-weight: 800;
}

.header-desc {
  margin-top: 10rpx;
  color: rgba(248, 252, 244, 0.78);
  font-size: 23rpx;
  line-height: 1.6;
}

.header-count {
  color: var(--cart-red);
  background: rgba(237, 249, 242, 0.96);
  flex-shrink: 0;
}

.header-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 18rpx;
}

.header-meta-pill {
  color: #edf4e7;
  background: rgba(255, 255, 255, 0.12);
}

.summary-card {
  margin: 16rpx 18rpx 0;
  padding: 22rpx;
  border-radius: 28rpx;
  background: rgba(255, 252, 247, 0.96);
  box-shadow: 0 18rpx 40rpx rgba(67, 72, 57, 0.08);
}

.summary-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18rpx;
}

.summary-title {
  color: #22301f;
  font-size: 30rpx;
  font-weight: 800;
}

.summary-desc {
  margin-top: 8rpx;
  color: #788373;
  font-size: 22rpx;
  line-height: 1.6;
}

.summary-clear {
  color: #4c6842;
  font-size: 22rpx;
  font-weight: 700;
  padding-top: 4rpx;
  white-space: nowrap;
}

.summary-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.summary-pill {
  max-width: 100%;
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  background: #e8f8ee;
  color: #07c160;
  font-size: 21rpx;
  font-weight: 700;
}

.cart-list {
  padding: 16rpx 18rpx 0;
}

.cart-item {
  background: rgba(255, 252, 247, 0.96);
  border-radius: 26rpx;
  padding: 22rpx;
  margin-bottom: 14rpx;
  box-shadow: 0 16rpx 30rpx rgba(79, 86, 62, 0.06);
}

.item-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.item-name {
  flex: 1;
  min-width: 0;
  font-size: 30rpx;
  font-weight: 800;
  color: #22301f;
}

.item-badge {
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  background: #e8f8ee;
  color: #07c160;
  font-size: 20rpx;
  font-weight: 700;
  flex-shrink: 0;
}

.item-price {
  font-size: 24rpx;
  color: #788373;
  margin-top: 8rpx;
}

.item-subtotal {
  font-size: 34rpx;
  font-weight: 800;
  color: var(--price);
  flex-shrink: 0;
}

.item-ops {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 18rpx;
  gap: 16rpx;
}

.qty-stepper {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  padding: 8rpx;
  border-radius: 999rpx;
  background: #eef8f1;
}

.qty-btn {
  width: 52rpx;
  height: 52rpx;
  border-radius: 999rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #233021;
  line-height: 1;
}

.qty-num {
  width: 64rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
  color: #233021;
}

.remark-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10rpx;
  min-width: 0;
}

.remark-input {
  flex: 1;
  height: 58rpx;
  background: #f5f6f1;
  border-radius: 16rpx;
  padding: 0 16rpx;
  font-size: 24rpx;
  color: #22301f;
}

.remark-save {
  flex-shrink: 0;
  min-width: 88rpx;
  height: 58rpx;
  line-height: 58rpx;
  text-align: center;
  border-radius: 16rpx;
  background: #e8f8ee;
  color: #07a857;
  font-size: 22rpx;
  font-weight: 700;
}

.bottom-bar {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: calc(24rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 10rpx;
  border-radius: 28rpx;
  background: rgba(255, 251, 246, 0.8);
  border: 1rpx solid rgba(255, 255, 255, 0.68);
  box-shadow: 0 24rpx 46rpx rgba(55, 66, 48, 0.14);
  backdrop-filter: blur(18rpx);
  z-index: 100;
}

.bar-info {
  flex: 1;
  min-width: 0;
  padding: 12rpx 16rpx;
  border-radius: 20rpx;
  background: rgba(243, 248, 238, 0.76);
}

.coupon-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 10rpx;
  min-width: 0;
}

.coupon-row-label {
  font-size: 24rpx;
  color: #768271;
  flex-shrink: 0;
}

.coupon-row-value {
  color: var(--cart-red);
  font-size: 24rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bar-total-wrap {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.bar-total-label {
  font-size: 24rpx;
  color: #768271;
}

.bar-total-price {
  font-size: 40rpx;
  font-weight: 800;
  color: var(--price);
}

.bar-submit {
  width: 220rpx;
  height: 80rpx;
  border-radius: 24rpx;
  font-size: 30rpx;
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  color: #ffffff;
  border: 1rpx solid rgba(7, 193, 96, 0.28);
  box-shadow: 0 12rpx 26rpx rgba(7, 193, 96, 0.18);
}

.cart-empty-action {
  min-width: 240rpx;
  height: 80rpx;
  padding: 0 40rpx;
  border-radius: 24rpx;
  font-size: 30rpx;
  background: linear-gradient(180deg, #f5fcf8 0%, #e8f8ee 100%);
  color: #07c160;
  border: 1rpx solid rgba(7, 193, 96, 0.16);
  box-shadow: 0 8rpx 18rpx rgba(5, 122, 61, 0.06);
}
</style>