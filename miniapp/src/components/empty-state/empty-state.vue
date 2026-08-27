<template>
  <view class="empty-state">
    <image v-if="icon" class="empty-icon" :src="icon" mode="aspectFit" />
    <text class="empty-text">{{ text }}</text>
    <button
      v-if="actionText"
      class="btn-primary btn-sm"
      :class="actionClass"
      @tap="$emit('action')"
    >{{ actionText }}</button>
  </view>
</template>

<script setup>
// 空状态占位
defineProps({
  text: { type: String, default: '暂无数据' },
  icon: { type: String, default: '' },
  actionText: { type: String, default: '' },
  actionClass: { type: String, default: '' }
});

defineEmits(['action']);
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}
.empty-icon {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 24rpx;
  opacity: 0.35;
}
.empty-text {
  font-size: 28rpx;
  color: var(--text-secondary);
  margin-bottom: 32rpx;
}
</style>