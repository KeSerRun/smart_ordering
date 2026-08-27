<template>
  <view class="profile-page">
    <view class="profile-page__glow profile-page__glow-a"></view>
    <view class="profile-page__glow profile-page__glow-b"></view>

    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content" :style="{ height: navBarHeight + 'px' }">
        <view class="nav-title">我的</view>
      </view>
    </view>

    <view class="profile-hero" v-if="banners.length > 0">
      <view class="profile-hero__banner-stage" v-if="banners.length > 0">
        <swiper class="profile-hero__banner-swiper" circular autoplay interval="4200" duration="450"
          indicator-dots indicator-color="rgba(237, 249, 242, 0.24)" indicator-active-color="#fff7f2">
          <swiper-item v-for="b in banners" :key="b.id">
            <view class="profile-hero__banner-card" @tap="openBanner(b)">
              <image class="profile-hero__banner-image" :src="b.imageUrl" mode="aspectFill" lazy-load />
              <view class="profile-hero__banner-overlay"></view>
            </view>
          </swiper-item>
        </swiper>
      </view>
    </view>

    <view class="identity-card" :class="{ 'identity-card--overlap': banners.length > 0 }">
      <template v-if="!loggedIn">
        <view class="identity-card__title">注册/登录</view>
        <view class="identity-card__desc">注册会员享受更多专属特权</view>
        <view class="agreement-row" @tap="toggleAgreeProtocol">
          <view class="agreement-check" :class="{ checked: agreeProtocol }">
            <text v-if="agreeProtocol">✓</text>
          </view>
          <view class="agreement-text">
            已阅读并同意
            <text class="agreement-link" @tap.stop="openUserAgreement">《用户协议》</text>
            和
            <text class="agreement-link" @tap.stop="openPrivacyPolicy">《隐私政策》</text>
          </view>
        </view>
      </template>
      <template v-else>
        <view class="identity-card__title">会员已登录</view>
        <view class="identity-card__desc">订单、优惠券、评价和会员服务都可以在这里查看</view>
      </template>
    </view>

    <view class="login-section" v-if="!loggedIn">
      <button
        class="btn-primary identity-card__btn"
        :class="{ 'identity-card__btn--disabled': !agreeProtocol }"
        :open-type="agreeProtocol ? 'getPhoneNumber' : ''"
        @tap="handlePhoneLoginTap"
        @getphonenumber="handlePhoneLogin"
      >
        手机号快捷登录
      </button>
    </view>

    <view class="promo-banner" v-if="banners.length === 0">
      <view class="promo-banner__frame">
        <view class="promo-banner__copy">
          <view class="promo-banner__eyebrow">会员服务</view>
          <view class="promo-banner__title">会员专享</view>
          <view class="promo-banner__desc">优惠券、会员积分、评价记录都在这里统一管理</view>
          <view class="promo-banner__tag">云点餐会员中心</view>
        </view>
        <view class="promo-banner__snack">
          <view class="promo-banner__snack-a"></view>
          <view class="promo-banner__snack-b"></view>
          <view class="promo-banner__snack-c"></view>
          <view class="promo-banner__crumb crumb-a"></view>
          <view class="promo-banner__crumb crumb-b"></view>
          <view class="promo-banner__crumb crumb-c"></view>
        </view>
      </view>
    </view>

    <view class="quick-card">
      <view class="quick-grid">
        <view class="quick-item" @tap="goOrder">
          <view class="quick-item__icon">单</view>
          <view class="quick-item__label">订单</view>
        </view>
        <view class="quick-item" @tap="goCoupon">
          <view class="quick-item__icon">券</view>
          <view class="quick-item__label">优惠设置</view>
        </view>
        <view class="quick-item" @tap="goMember">
          <view class="quick-item__icon">会</view>
          <view class="quick-item__label">完善资料</view>
        </view>
        <view class="quick-item" @tap="goFeedback">
          <view class="quick-item__icon">反</view>
          <view class="quick-item__label">意见反馈</view>
        </view>
      </view>
    </view>

    <template v-if="loggedIn">
      <view class="menu-section">
        <view class="menu-group">
          <view class="menu-group__title">更多服务</view>
          <view class="menu-item" @tap="goMyReview">
            <view class="menu-left">
              <text class="menu-icon">评</text>
              <text class="menu-label">我的评价</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
          <view class="menu-item" @tap="goFeedback">
            <view class="menu-left">
              <text class="menu-icon">反</text>
              <text class="menu-label">意见反馈</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
          <view class="menu-item" @tap="goAbout">
            <view class="menu-left">
              <text class="menu-icon">关</text>
              <text class="menu-label">关于我们</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
          <view class="menu-item" @tap="clearCache">
            <view class="menu-left">
              <text class="menu-icon">清</text>
              <text class="menu-label">清除缓存</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
        </view>
      </view>

      <view class="logout-section">
        <button class="logout-btn" @tap="handleLogout">退出登录</button>
      </view>
    </template>
  </view>
</template>

<script setup>
// 我的页 / 登录
import { reactive, toRefs } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getBannerList } from '@/api/banner';
import { isLoggedIn, wxLogin, phoneLogin } from '@/utils/auth';
import { KEYS, get, remove } from '@/utils/storage';
import { calcNavBar } from '@/utils/nav';

const state = reactive({
  statusBarHeight: 0,
  navBarHeight: 44,
  loggedIn: false,
  userInfo: null,
  agreeProtocol: false,
  banners: []
});

const { statusBarHeight, navBarHeight, loggedIn, agreeProtocol, banners } = toRefs(state);

onLoad(() => {
  initNavBar();
});

onShow(() => {
  loadBanners();
  state.loggedIn = isLoggedIn();
  state.userInfo = get(KEYS.USER_INFO) || null;
});

async function loadBanners() {
  try {
    const banners = await getBannerList('PROFILE_HERO');
    state.banners = Array.isArray(banners) ? banners : [];
  } catch (err) {
    if (!Array.isArray(state.banners) || state.banners.length === 0) {
      state.banners = [];
    }
  }
}

function initNavBar() {
  const { statusBarHeight, navBarHeight } = calcNavBar();
  state.statusBarHeight = statusBarHeight;
  state.navBarHeight = navBarHeight;
}

// ===== 登录 =====
async function handlePhoneLogin(e) {
  if (!state.agreeProtocol) {
    uni.showToast({ title: '请先勾选用户协议与隐私政策', icon: 'none' });
    return;
  }
  if (!e.detail.code) {
    uni.showToast({ title: e.detail.errMsg || '请授权手机号', icon: 'none' });
    return;
  }
  uni.showLoading({ title: '登录中', mask: true });
  try {
    const code = await wxLogin();
    await phoneLogin(code, e.detail.code);
    state.loggedIn = true;
    state.userInfo = get(KEYS.USER_INFO) || null;
    uni.showToast({ title: '登录成功', icon: 'none' });
  } catch (err) {
    uni.showToast({ title: err.message || '登录失败', icon: 'none', duration: 2000 });
  } finally {
    uni.hideLoading();
  }
}

function handlePhoneLoginTap() {
  if (state.agreeProtocol) return;
  uni.showToast({ title: '请先勾选用户协议与隐私政策', icon: 'none' });
}

function toggleAgreeProtocol() {
  state.agreeProtocol = !state.agreeProtocol;
}

function openUserAgreement() {
  uni.showModal({
    title: '用户协议',
    content: '登录前请阅读并同意《用户协议》。当前先使用说明弹窗占位，后续可接正式协议页。',
    showCancel: false
  });
}

function openPrivacyPolicy() {
  uni.showModal({
    title: '隐私政策',
    content: '登录前请阅读并同意《隐私政策》。当前先使用说明弹窗占位，后续可接正式隐私政策页。',
    showCancel: false
  });
}

function handleLogout() {
  uni.showModal({
    title: '退出登录',
    content: '确定退出当前账号吗？退出后仍可继续浏览和点餐。',
    success: (res) => {
      if (!res.confirm) return;
      remove(KEYS.TOKEN);
      remove(KEYS.OPENID);
      remove(KEYS.USER_INFO);
      state.loggedIn = false;
      state.userInfo = null;
      uni.showToast({ title: '已退出登录', icon: 'none' });
    }
  });
}

// ===== 导航 =====
function goMyReview() {
  uni.navigateTo({ url: '/pages/my-review/index' });
}

function goOrder() {
  uni.switchTab({ url: '/pages/order/index' });
}

function goCoupon() {
  uni.navigateTo({ url: '/pages/coupon/index' });
}

function goMember() {
  uni.navigateTo({ url: '/pages/member/index' });
}

function goFeedback() {
  uni.navigateTo({ url: '/pages/feedback/index' });
}

function goAbout() {
  uni.showModal({
    title: '关于云点餐',
    content: '云点餐 — 智能堂食点单系统\n版本 1.0.0\n\n致力于为餐厅提供便捷的扫码点餐体验',
    showCancel: false
  });
}

function openBanner(banner) {
  const targetPath = banner.targetPath || '';
  const actionType = Number(banner.actionType || 0);
  if (!targetPath || actionType === 0) return;
  if (actionType === 2) {
    uni.switchTab({ url: targetPath });
    return;
  }
  uni.navigateTo({ url: targetPath });
}

function clearCache() {
  uni.showModal({
    title: '清除缓存',
    content: '确定要清除本地缓存吗？这会清除桌台信息和本地临时记录。',
    success: (res) => {
      if (res.confirm) {
        remove(KEYS.TABLE);
        remove(KEYS.ORDER_ID);
        remove(KEYS.ORDERED_DISH_IDS);
        remove(KEYS.MOCK_PAID_ORDER_IDS);
        remove(KEYS.REVIEWED_ORDER_IDS);
        remove(KEYS.PERSON_COUNT);
        uni.showToast({ title: '本地缓存已清除', icon: 'none' });
      }
    }
  });
}
</script>

<style scoped>
.profile-page {
  --profile-accent: #07c160;
  --profile-accent-deep: #057a3d;
  --profile-bg: #f3f1f1;
  --profile-surface: rgba(255, 252, 247, 0.94);
  --profile-text: #261718;
  --profile-subtext: #7c6f6a;

  min-height: 100vh;
  position: relative;
  padding-bottom: calc(48rpx + env(safe-area-inset-bottom));
  background:
    radial-gradient(circle at left 78%, rgba(7, 193, 96, 0.05), transparent 22%),
    linear-gradient(180deg, #f6f3f1 0%, var(--profile-bg) 62%, #eeeeef 100%);
  box-sizing: border-box;
}

.profile-page__glow {
  position: absolute;
  border-radius: 999rpx;
  pointer-events: none;
  filter: blur(8rpx);
}

.profile-page__glow-a {
  top: 300rpx;
  left: -120rpx;
  width: 240rpx;
  height: 240rpx;
  background: rgba(230, 214, 191, 0.18);
}

.profile-page__glow-b {
  right: -120rpx;
  top: 460rpx;
  width: 260rpx;
  height: 260rpx;
  background: rgba(7, 193, 96, 0.06);
}

.nav-bar {
  position: relative;
  z-index: 2;
  padding-left: 28rpx;
  padding-right: 28rpx;
}

.nav-content {
  display: flex;
  align-items: center;
}

.nav-title {
  color: #201112;
  font-size: 38rpx;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.profile-hero,
.identity-card,
.promo-banner,
.quick-card,
.menu-section {
  position: relative;
  z-index: 2;
  margin-left: 24rpx;
  margin-right: 24rpx;
}

.profile-hero {
  margin-top: 12rpx;
  margin-left: 0;
  margin-right: 0;
  padding: 0;
  border-radius: 0;
  background: transparent;
  overflow: hidden;
}

.profile-hero::after {
  content: none;
}

.profile-hero__banner-stage {
  position: relative;
  z-index: 2;
}

.profile-hero__banner-swiper {
  height: 256rpx;
}

.profile-hero__banner-card {
  position: relative;
  height: 256rpx;
  overflow: hidden;
  border-radius: 0 0 32rpx 32rpx;
  background: rgba(255, 255, 255, 0.16);
  box-shadow: 0 22rpx 36rpx rgba(5, 122, 61, 0.16);
}

.profile-hero__banner-image {
  width: 100%;
  height: 100%;
  display: block;
}

.profile-hero__banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(33, 16, 18, 0.06) 0%, rgba(33, 16, 18, 0) 42%, rgba(33, 16, 18, 0.18) 100%);
}

.identity-card {
  margin-top: 18rpx;
  padding: 28rpx 34rpx 32rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 24rpx 42rpx rgba(58, 34, 29, 0.11);
}

.identity-card--overlap {
  margin-top: -30rpx;
}

.identity-card__title {
  color: #231516;
  font-size: 42rpx;
  font-weight: 800;
  line-height: 1.25;
}

.identity-card__desc {
  margin-top: 10rpx;
  color: #a29a95;
  font-size: 20rpx;
  line-height: 1.6;
}

.agreement-row {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-top: 22rpx;
}

.agreement-check {
  width: 28rpx;
  height: 28rpx;
  margin-top: 2rpx;
  border-radius: 999rpx;
  border: 2rpx solid rgba(7, 193, 96, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff7f2;
  font-size: 18rpx;
  font-weight: 800;
  box-sizing: border-box;
  flex-shrink: 0;
}

.agreement-check.checked {
  border-color: #07c160;
  background: #07c160;
}

.agreement-text {
  color: #8f8580;
  font-size: 20rpx;
  line-height: 1.6;
}

.agreement-link {
  color: #07c160;
  font-weight: 700;
}

.identity-card__btn {
  flex: 1;
  width: auto;
  height: 84rpx;
  line-height: 84rpx;
  margin: 0;
  padding: 0;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 800;
  box-sizing: border-box;
}

.identity-card__btn--disabled {
  background: rgba(255, 255, 255, 0.96) !important;
  color: #07c160 !important;
  border-color: rgba(7, 193, 96, 0.42) !important;
  box-shadow: 0 8rpx 18rpx rgba(7, 193, 96, 0.08) !important;
  opacity: 1;
}

.promo-banner {
  margin-top: 18rpx;
}

.promo-banner__frame {
  position: relative;
  overflow: hidden;
  border-radius: 26rpx;
  min-height: 164rpx;
  padding: 0;
  background:
    linear-gradient(135deg, #057a3d 0%, #dff3b6 54%, #fff7d8 100%);
  box-shadow: 0 14rpx 28rpx rgba(5, 122, 61, 0.12);
  border: 8rpx solid #07a857;
}

.promo-banner__copy {
  position: relative;
  z-index: 2;
  width: 60%;
  padding: 18rpx 22rpx 18rpx;
}

.promo-banner__eyebrow {
  display: inline-flex;
  align-items: center;
  min-height: 34rpx;
  padding: 0 12rpx;
  border-radius: 999rpx;
  background: rgba(7, 193, 96, 0.14);
  color: #07c160;
  font-size: 18rpx;
  font-weight: 700;
}

.promo-banner__title {
  margin-top: 10rpx;
  color: #43221f;
  font-size: 48rpx;
  font-weight: 800;
  line-height: 1.08;
  letter-spacing: 0.04em;
}

.promo-banner__desc {
  margin-top: 8rpx;
  color: rgba(67, 34, 31, 0.74);
  font-size: 18rpx;
  line-height: 1.5;
}

.promo-banner__tag {
  display: inline-flex;
  align-items: center;
  margin-top: 12rpx;
  min-height: 34rpx;
  padding: 0 12rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.42);
  color: #07c160;
  font-size: 18rpx;
  font-weight: 700;
}

.promo-banner__snack {
  position: absolute;
  right: 22rpx;
  top: 24rpx;
  width: 210rpx;
  height: 120rpx;
}

.promo-banner__snack-a,
.promo-banner__snack-b,
.promo-banner__snack-c {
  position: absolute;
  border-radius: 22rpx;
  background: linear-gradient(180deg, #fff1ca 0%, #f6ca4f 100%);
  border: 2rpx solid rgba(7, 193, 96, 0.18);
  box-shadow: 0 12rpx 20rpx rgba(124, 46, 18, 0.10);
}

.promo-banner__snack-a {
  left: 20rpx;
  top: 8rpx;
  width: 62rpx;
  height: 106rpx;
  transform: rotate(-14deg);
}

.promo-banner__snack-b {
  left: 86rpx;
  top: 0;
  width: 68rpx;
  height: 116rpx;
  transform: rotate(6deg);
}

.promo-banner__snack-c {
  right: 0;
  top: 16rpx;
  width: 62rpx;
  height: 100rpx;
  transform: rotate(18deg);
}

.promo-banner__crumb {
  position: absolute;
  width: 10rpx;
  height: 10rpx;
  border-radius: 999rpx;
  background: rgba(202, 116, 33, 0.58);
}

.crumb-a {
  left: 74rpx;
  top: 24rpx;
}

.crumb-b {
  left: 150rpx;
  top: 92rpx;
}

.crumb-c {
  right: 52rpx;
  top: 48rpx;
}

.quick-card {
  margin-top: 18rpx;
  padding: 26rpx 16rpx 22rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14rpx 28rpx rgba(5, 122, 61, 0.08);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 6rpx;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 4rpx 0;
}

.quick-item__icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #f5fcf8 0%, #e8f8ee 100%);
  border: 2rpx solid rgba(7, 193, 96, 0.18);
  color: #07c160;
  font-size: 18rpx;
  font-weight: 700;
  box-shadow: 0 8rpx 16rpx rgba(5, 122, 61, 0.08);
}

.quick-item__label {
  margin-top: 10rpx;
  color: #07a857;
  font-size: 18rpx;
  font-weight: 700;
  line-height: 1.3;
}

.menu-section {
  margin-top: 24rpx;
}

.login-section,
.logout-section {
  position: relative;
  z-index: 2;
  margin: 22rpx 24rpx 0;
  display: flex;
}

.logout-btn {
  flex: 1;
  width: auto;
  height: 84rpx;
  line-height: 84rpx;
  margin: 0;
  padding: 0;
  border: 1rpx solid rgba(100, 116, 139, 0.18);
  border-radius: 24rpx;
  background: linear-gradient(180deg, #f8fafc 0%, #edf1f5 100%);
  color: #64748b;
  font-size: 28rpx;
  font-weight: 700;
  box-shadow: 0 8rpx 18rpx rgba(71, 85, 105, 0.06);
  box-sizing: border-box;
}

.menu-group {
  padding: 16rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14rpx 30rpx rgba(67, 72, 57, 0.06);
}

.menu-group + .menu-group {
  margin-top: 18rpx;
}

.menu-group__title {
  padding: 8rpx 14rpx 18rpx;
  color: #07c160;
  font-size: 22rpx;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 22rpx 18rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.78);
}

.menu-item + .menu-item {
  margin-top: 12rpx;
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  min-width: 0;
}

.menu-icon {
  width: 54rpx;
  height: 54rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e8f8ee;
  color: var(--profile-accent);
  font-size: 22rpx;
  font-weight: 800;
  flex-shrink: 0;
}

.menu-label {
  color: var(--profile-text);
  font-size: 28rpx;
  font-weight: 700;
}

.menu-arrow {
  color: #b29e98;
  font-size: 34rpx;
  line-height: 1;
}
</style>