<template>
  <view class="app-navbar-placeholder" :style="{ height: totalHeight + 'px' }">
    <view
      class="app-navbar"
      :style="{ height: totalHeight + 'px', backgroundColor: backgroundColor, color: textColor }"
    >
      <view
        class="app-navbar__body"
        :style="{ height: navBarHeight + 'px', paddingTop: statusBarHeight + 'px' }"
      >
        <view
          v-if="showBack"
          class="app-navbar__back"
          hover-class="app-navbar__back--pressed"
          @tap="handleBack"
          aria-label="返回"
        >
          <view class="app-navbar__back-icon"></view>
        </view>
        <view class="app-navbar__title">{{ title }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
// 自定义导航栏
import { ref, computed, onMounted } from 'vue';
import { calcNavBar } from '@/utils/nav';

const props = defineProps({
  title: { type: String, default: '' },
  backgroundColor: { type: String, default: '#fffdf9' },
  textColor: { type: String, default: '#251819' },
  showBack: { type: Boolean, default: true }
});

const statusBarHeight = ref(20);
const navBarHeight = ref(44);
const totalHeight = computed(() => statusBarHeight.value + navBarHeight.value);

onMounted(() => {
  const { statusBarHeight: sb, navBarHeight: nb } = calcNavBar();
  statusBarHeight.value = sb;
  navBarHeight.value = nb;
});

function handleBack() {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
    return;
  }
  uni.switchTab({ url: '/pages/index/index' });
}
</script>

<style scoped>
.app-navbar-placeholder {
  width: 100%;
  flex-shrink: 0;
}

.app-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  box-sizing: border-box;
  border-bottom: 1rpx solid rgba(31, 43, 29, 0.06);
}

.app-navbar__body {
  position: relative;
  display: flex;
  align-items: center;
  box-sizing: content-box;
}

.app-navbar__back {
  position: absolute;
  left: 12rpx;
  bottom: 0;
  width: 88rpx;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-navbar__back--pressed {
  opacity: 0.5;
}

.app-navbar__back-icon {
  width: 18rpx;
  height: 18rpx;
  border-left: 4rpx solid currentColor;
  border-bottom: 4rpx solid currentColor;
  transform: rotate(45deg);
}

.app-navbar__title {
  position: absolute;
  left: 112rpx;
  right: 190rpx;
  bottom: 0;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: inherit;
  font-size: 32rpx;
  font-weight: 600;
  line-height: 1.4;
  text-align: center;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>