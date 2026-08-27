<template>
  <view class="page">
    <app-navbar title="会员中心" />
    <view class="page-glow page-glow-a"></view>
    <view class="page-glow page-glow-b"></view>

    <view class="hero-card" v-if="center">
      <view class="hero-top">
        <view>
          <view class="hero-chip">会员中心</view>
          <view class="hero-title">{{ center.levelName || '普通会员' }}</view>
          <view class="hero-subtitle">会员编号 {{ center.memberNo }}</view>
        </view>
        <view class="hero-badge">VIP</view>
      </view>

      <view class="hero-stats">
        <view class="stat-card" @tap="goPoints">
          <view class="stat-value">{{ center.pointsBalance || 0 }}</view>
          <view class="stat-label">当前积分</view>
        </view>
        <view class="stat-card" @tap="goGrowth">
          <view class="stat-value">{{ center.growthValue || 0 }}</view>
          <view class="stat-label">成长值</view>
        </view>
        <view class="stat-card">
          <view class="stat-value">{{ center.totalAmountConsumed || 0 }}</view>
          <view class="stat-label">累计消费</view>
        </view>
      </view>

      <view class="progress-card">
        <view class="progress-head">
          <text>升级进度</text>
          <text v-if="center.pointsToNextLevel > 0">还差 {{ center.pointsToNextLevel }} 成长值升级 {{ center.nextLevelName }}</text>
          <text v-else>当前已是最高等级</text>
        </view>
        <view class="progress-track">
          <view class="progress-fill" :style="{ width: progressPercent(center) + '%' }"></view>
        </view>
        <view class="progress-foot">
          <text>当前 {{ center.growthValue || 0 }}</text>
          <text>{{ center.nextLevelThreshold || center.growthValue || 0 }}</text>
        </view>
      </view>

      <view class="benefit-card">
        <view class="benefit-head">
          <text>当前等级权益</text>
          <text>{{ center.pointsRate || 1 }} 倍积分 / {{ center.discountRate || 1 }} 折扣倍率</text>
        </view>
        <view class="benefit-tags" v-if="currentBenefitTags.length">
          <text class="benefit-tag" v-for="(tag, i) in currentBenefitTags" :key="i">{{ tag }}</text>
        </view>
        <view class="benefit-empty" v-else>当前等级暂未配置额外权益说明</view>
      </view>
    </view>

    <view class="action-row">
      <view class="action-card" @tap="goPoints">
        <view class="action-icon">积</view>
        <view>
          <view class="action-title">积分明细</view>
          <view class="action-desc">查看积分获取与变动记录</view>
        </view>
      </view>
      <view class="action-card" @tap="goGrowth">
        <view class="action-icon">长</view>
        <view>
          <view class="action-title">成长明细</view>
          <view class="action-desc">查看成长值累计与升级进度</view>
        </view>
      </view>
      <view class="action-card" @tap="goCoupon">
        <view class="action-icon">券</view>
        <view>
          <view class="action-title">我的优惠券</view>
          <view class="action-desc">查看已到账的生日券、等级券和兑换券</view>
        </view>
      </view>
    </view>

    <view class="section" v-if="benefitOverview">
      <view class="section-title">会员权益玩法</view>
      <view class="benefit-panel">
        <view class="benefit-panel__item">
          <view class="benefit-panel__title">积分抵现</view>
          <view class="benefit-panel__desc" v-if="benefitOverview.pointsDeductionRule && benefitOverview.pointsDeductionRule.enabled">
            {{ pointsDeductionSummary }}
          </view>
          <view class="benefit-panel__desc" v-else>当前未启用积分抵现</view>
        </view>

        <view class="benefit-panel__item" v-if="benefitOverview.exclusiveBenefit && benefitOverview.exclusiveBenefit.templateId">
          <view class="benefit-panel__head">
            <view>
              <view class="benefit-panel__title">{{ benefitOverview.exclusiveBenefit.levelName }} 专属券</view>
              <view class="benefit-panel__desc">{{ benefitOverview.exclusiveBenefit.templateName }}</view>
            </view>
            <button class="mini-btn" @tap="claimExclusiveCoupon" :disabled="!benefitOverview.exclusiveBenefit.claimable">
              {{ benefitOverview.exclusiveBenefit.claimable ? '立即领取' : benefitOverview.exclusiveBenefit.claimTip }}
            </button>
          </view>
        </view>
      </view>
    </view>

    <view class="section" v-if="benefitOverview && benefitOverview.exchangeCoupons && benefitOverview.exchangeCoupons.length">
      <view class="section-title">积分兑换优惠券</view>
      <view class="exchange-list">
        <view class="exchange-card" v-for="item in benefitOverview.exchangeCoupons" :key="item.id">
          <view class="exchange-card__main">
            <view class="exchange-card__title">{{ item.templateName }}</view>
            <view class="exchange-card__desc">{{ item.description || '兑换后自动发放到我的优惠券' }}</view>
            <view class="exchange-card__meta">已兑 {{ item.exchangedCount || 0 }} 次<span v-if="item.perUserLimit > 0"> / 上限 {{ item.perUserLimit }} 次</span></view>
          </view>
          <view class="exchange-card__side">
            <view class="exchange-card__cost">{{ item.pointsCost }} 积分</view>
            <button class="exchange-btn" @tap="exchangeCoupon(item)">立即兑换</button>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-title">等级说明</view>
      <view class="level-list">
        <view class="level-item" v-for="item in levels" :key="item.id">
          <view class="level-head">
            <text class="level-name">{{ item.levelName }}</text>
            <text class="level-threshold">成长值 {{ item.growthThreshold }}</text>
          </view>
          <view class="level-meta">
            <text>积分倍率 {{ item.pointsRate }}</text>
            <text>折扣倍率 {{ item.discountRate }}</text>
          </view>
          <view class="level-benefits" v-if="item.benefitConfig">{{ item.benefitConfig }}</view>
          <view class="level-remark" v-if="item.remark">{{ item.remark }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
// 会员中心
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getMemberCenter, getMemberLevels, getMemberBenefitOverview, exchangeCoupon as exchangeApi, claimExclusiveCoupon as claimApi } from '@/api/member';
import { isLoggedIn } from '@/utils/auth';

const loading = ref(false);
const center = ref(null);
const levels = ref([]);
const currentBenefitTags = ref([]);
const benefitOverview = ref(null);
const pointsDeductionSummary = ref('');

onShow(() => {
  if (!isLoggedIn()) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    uni.navigateBack({ delta: 1 });
    return;
  }
  loadData();
});

async function loadData() {
  loading.value = true;
  try {
    const [centerRes, levelsRes] = await Promise.all([
      getMemberCenter(),
      getMemberLevels()
    ]);
    const overview = await getMemberBenefitOverview().catch(() => null);
    const levelList = Array.isArray(levelsRes) ? levelsRes : [];
    const currentLevel = levelList.find((item) => Number(item.id) === Number(centerRes?.levelId || 0)) || null;
    center.value = centerRes || null;
    levels.value = levelList;
    currentBenefitTags.value = parseBenefitConfig(currentLevel ? currentLevel.benefitConfig : '');
    benefitOverview.value = overview || null;
    pointsDeductionSummary.value = buildPointsDeductionSummary(overview);
  } catch (err) {
    uni.showToast({ title: err.message || '加载会员信息失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function goPoints() {
  uni.navigateTo({ url: '/pages/member-points/index' });
}

function goGrowth() {
  uni.navigateTo({ url: '/pages/member-growth/index' });
}

function goCoupon() {
  uni.navigateTo({ url: '/pages/coupon/index' });
}

async function exchangeCoupon(item) {
  const id = item && item.id;
  const cost = item && item.pointsCost;
  if (!id) return;
  uni.showLoading({ title: '兑换中', mask: true });
  try {
    await exchangeApi(id);
    uni.showToast({ title: `已兑换，消耗 ${cost || 0} 积分`, icon: 'none' });
    loadData();
  } catch (err) {
    uni.showToast({ title: err.message || '兑换失败', icon: 'none' });
  } finally {
    uni.hideLoading();
  }
}

async function claimExclusiveCoupon() {
  uni.showLoading({ title: '领取中', mask: true });
  try {
    await claimApi();
    uni.showToast({ title: '专属券已到账', icon: 'none' });
    loadData();
  } catch (err) {
    uni.showToast({ title: err.message || '领取失败', icon: 'none' });
  } finally {
    uni.hideLoading();
  }
}

function progressPercent(c) {
  if (!c) return 100;
  if (c.pointsToNextLevel > 0 && c.nextLevelThreshold) {
    return Math.min(100, Number((c.growthValue / c.nextLevelThreshold) * 100) || 0);
  }
  return 100;
}

function buildPointsDeductionSummary(overview) {
  const rule = overview && overview.pointsDeductionRule;
  if (!rule || !rule.enabled) return '';
  const ratio = Number(rule.maxDeductionRatio || 0);
  const ratioText = Number.isFinite(ratio) ? Math.round(ratio * 100) : 0;
  return `每 ${rule.pointsPerStep || 0} 积分抵 ${rule.amountPerStep || 0} 元，单笔最多抵 ${ratioText}%`;
}

function parseBenefitConfig(raw) {
  if (!raw) return [];
  if (Array.isArray(raw)) {
    return raw.map((item) => String(item).trim()).filter(Boolean);
  }
  if (typeof raw === 'string') {
    const text = raw.trim();
    if (!text) return [];
    try {
      const parsed = JSON.parse(text);
      if (Array.isArray(parsed)) {
        return parsed.map((item) => String(item).trim()).filter(Boolean);
      }
      if (parsed && typeof parsed === 'object') {
        return Object.entries(parsed).map(([key, value]) => `${key}：${value}`).filter(Boolean);
      }
    } catch (err) {
      return text.split(/[,\n，；;|]/).map((item) => item.trim()).filter(Boolean);
    }
    return [text];
  }
  return [String(raw)];
}
</script>

<style scoped>
.page {
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

.hero-card {
  position: relative;
  z-index: 2;
  padding: 30rpx;
  border-radius: 32rpx;
  background: linear-gradient(145deg, #057a3d 0%, #07c160 62%, #18d071 100%);
  color: #fff;
  box-shadow: 0 24rpx 44rpx rgba(5, 122, 61, 0.20);
}

.hero-top,
.hero-stats,
.progress-head,
.progress-foot,
.level-head,
.level-meta,
.action-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42rpx;
  padding: 0 14rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.14);
  font-size: 20rpx;
  font-weight: 700;
}

.hero-title {
  margin-top: 16rpx;
  font-size: 42rpx;
  font-weight: 800;
}

.hero-subtitle {
  margin-top: 8rpx;
  font-size: 24rpx;
  opacity: 0.82;
}

.hero-badge {
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.18);
  font-size: 22rpx;
  letter-spacing: 2rpx;
}

.hero-stats {
  gap: 16rpx;
  margin-top: 28rpx;
}

.stat-card {
  flex: 1;
  padding: 24rpx 18rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.16);
  text-align: center;
}

.stat-value {
  font-size: 38rpx;
  font-weight: 800;
}

.stat-label {
  margin-top: 8rpx;
  font-size: 22rpx;
  opacity: 0.85;
}

.progress-card,
.benefit-card {
  margin-top: 24rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.14);
}

.benefit-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  font-size: 22rpx;
}

.benefit-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.benefit-tag {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.18);
  font-size: 22rpx;
}

.benefit-empty {
  margin-top: 18rpx;
  font-size: 22rpx;
  opacity: 0.82;
}

.progress-head,
.progress-foot {
  font-size: 22rpx;
}

.progress-track {
  height: 16rpx;
  margin: 18rpx 0 14rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.22);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 999rpx;
  background: linear-gradient(90deg, #ffe8e3 0%, #ffffff 100%);
}

.action-row {
  margin-top: 24rpx;
}

.action-card {
  padding: 28rpx 24rpx;
  margin-bottom: 16rpx;
  border-radius: 24rpx;
  background: rgba(255, 252, 247, 0.96);
  box-shadow: 0 14rpx 34rpx rgba(57, 36, 29, 0.06);
}

.action-icon {
  width: 76rpx;
  height: 76rpx;
  margin-right: 20rpx;
  border-radius: 22rpx;
  background: #e8f8ee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #07c160;
  font-weight: 800;
}

.action-card > view:last-child {
  flex: 1;
}

.action-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #22301f;
}

.action-desc {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #788373;
}

.section {
  margin-top: 12rpx;
}

.section-title {
  margin: 24rpx 6rpx 18rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #22301f;
}

.level-list,
.benefit-panel,
.exchange-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.level-item,
.benefit-panel__item,
.exchange-card {
  padding: 24rpx;
  border-radius: 24rpx;
  background: rgba(255, 252, 247, 0.96);
  box-shadow: 0 12rpx 28rpx rgba(40, 28, 23, 0.05);
}

.benefit-panel__head,
.exchange-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.level-name,
.benefit-panel__title,
.exchange-card__title {
  font-size: 30rpx;
  font-weight: 800;
  color: #22301f;
}

.level-threshold,
.level-meta,
.level-remark,
.level-benefits,
.benefit-panel__desc,
.exchange-card__desc,
.exchange-card__meta {
  font-size: 22rpx;
  color: #788373;
}

.level-meta {
  margin-top: 12rpx;
}

.level-benefits,
.level-remark,
.benefit-panel__desc,
.exchange-card__desc,
.exchange-card__meta {
  margin-top: 10rpx;
  line-height: 1.7;
}

.mini-btn,
.exchange-btn {
  min-width: 168rpx;
  height: 68rpx;
  padding: 0 22rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  color: #ffffff;
  border: 1rpx solid rgba(7, 193, 96, 0.28);
  font-size: 24rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 26rpx rgba(7, 193, 96, 0.18);
}

.mini-btn[disabled],
.exchange-btn[disabled] {
  opacity: 0.55;
}

.exchange-card__main {
  flex: 1;
  min-width: 0;
}

.exchange-card__side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12rpx;
}

.exchange-card__cost {
  font-size: 30rpx;
  font-weight: 800;
  color: #07c160;
}
</style>