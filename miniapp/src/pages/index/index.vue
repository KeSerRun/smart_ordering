<template>
  <view class="index-page">
    <!-- 顶部装修区 -->
    <view class="hero">
      <view class="hero-tag">{{ heroTag }}</view>
      <view class="hero-title">{{ heroTitle }}</view>
      <view class="hero-desc">{{ heroDesc }}</view>
    </view>

    <!-- 首页轮播 -->
    <swiper v-if="banners.length" class="banner" circular autoplay
      indicator-dots indicator-active-color="#07c160">
      <swiper-item v-for="(b, i) in banners" :key="i" @click="openBanner(b)">
        <image class="banner-img" :src="b.imageUrl || ''" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <!-- 分类快捷入口 -->
    <view class="section" v-if="categories.length">
      <view class="section-title">随便吃点</view>
      <view class="cat-grid">
        <view v-for="c in categories" :key="c.id" class="cat-item" @click="openCategory(c)">
          <image v-if="c.imageUrl" class="cat-img" :src="c.imageUrl" mode="aspectFill" />
          <view class="cat-rank">{{ c.sort || 0 }}</view>
          <view class="cat-name">{{ c.name }}</view>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="footer">
      <button class="btn-primary" @click="goMenu">进入点餐</button>
    </view>
  </view>
</template>

<script setup>
// 首页
// 完整桌台绑定 / 登录面板 / 优惠券预览等逻辑将在后续阶段迁移
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getBannerList } from '@/api/banner';
import { getCategoryList } from '@/api/dish';

const banners = ref([]);
const categories = ref([]);
const heroTag = ref('堂食点餐');
const heroTitle = ref('今天吃点招牌热菜');
const heroDesc = ref('首页先看活动、领券，再去点餐。');

onLoad(async () => {
  try {
    const list = await getBannerList('HOME');
    banners.value = Array.isArray(list) ? list : [];
  } catch (err) {
    banners.value = [];
  }
  try {
    const list = await getCategoryList();
    categories.value = Array.isArray(list) ? list.slice(0, 8) : [];
  } catch (err) {
    categories.value = [];
  }
});

function openCategory() {
  uni.switchTab({ url: '/pages/menu/index' });
}

function goMenu() {
  uni.switchTab({ url: '/pages/menu/index' });
}

function openBanner(b) {
  const targetPath = b.targetPath || '';
  const actionType = Number(b.actionType || 0);
  if (!targetPath || actionType === 0) return;
  if (actionType === 2) {
    uni.switchTab({ url: targetPath });
    return;
  }
  uni.navigateTo({ url: targetPath });
}
</script>

<style scoped>
.index-page {
  min-height: 100vh;
  padding-bottom: 40rpx;
}
.hero {
  padding: 40rpx 32rpx 24rpx;
}
.hero-tag {
  display: inline-flex;
  padding: 6rpx 16rpx;
  font-size: 20rpx;
  color: #fff;
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  border-radius: 20rpx;
}
.hero-title {
  margin-top: 16rpx;
  font-size: 44rpx;
  font-weight: 800;
  color: var(--text-primary);
}
.hero-desc {
  margin-top: 8rpx;
  font-size: 26rpx;
  color: var(--text-secondary);
}
.banner {
  height: 320rpx;
  margin: 0 24rpx;
  border-radius: 24rpx;
  overflow: hidden;
}
.banner-img {
  width: 100%;
  height: 100%;
}
.section {
  margin: 32rpx 24rpx 0;
}
.section-title {
  font-size: 30rpx;
  font-weight: 700;
  margin-bottom: 20rpx;
}
.cat-grid {
  display: flex;
  flex-wrap: wrap;
}
.cat-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 0;
}
.cat-img {
  width: 96rpx;
  height: 96rpx;
  border-radius: 20rpx;
  background: var(--primary-light);
}
.cat-rank {
  font-size: 20rpx;
  color: var(--text-muted);
}
.cat-name {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: var(--text-primary);
}
.footer {
  margin: 48rpx 32rpx 0;
}
</style>