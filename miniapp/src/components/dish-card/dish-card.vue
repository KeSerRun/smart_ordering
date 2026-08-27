<template>
  <view
    class="dish-card"
    :class="{ 'sold-out': dish.status === 0 }"
    @tap="$emit('tap', dish)"
  >
    <image class="dish-img" :src="dish.image || dish.imageUrl || ''" mode="aspectFill" lazy-load />
    <view class="dish-info">
      <view class="dish-name text-ellipsis">{{ dish.name }}</view>
      <view class="dish-desc text-ellipsis" v-if="dish.description">{{ dish.description }}</view>
      <view class="dish-tags" v-if="dish.tags && dish.tags.length">
        <text class="tag tag-hot" v-for="(t, i) in dish.tags.slice(0, 2)" :key="i">{{ t }}</text>
      </view>
      <view class="dish-bottom">
        <view class="dish-price">
          <text class="price-symbol">¥</text>
          <text class="price-num">{{ dish.price }}</text>
        </view>
        <view
          class="add-btn"
          :class="{ disabled: dish.status === 0 }"
          v-if="showAddBtn"
          @tap.stop="onAdd"
        >
          <text v-if="dish.status === 0">售罄</text>
          <text v-else>+</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
// 菜品卡片
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  dish: { type: Object, default: () => ({}) },
  showAddBtn: { type: Boolean, default: true }
});

const emit = defineEmits(['tap', 'add']);

function onTap() {
  emit('tap', props.dish);
}

function onAdd() {
  if (props.dish.status === 0) return;
  emit('add', props.dish);
}
</script>

<style scoped>
.dish-card {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid var(--border);
}
.dish-card:last-child {
  border-bottom: none;
}
.dish-card.sold-out {
  opacity: 0.5;
}
.dish-img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
  background: #f0f0f0;
}
.dish-info {
  flex: 1;
  margin-left: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}
.dish-name {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-primary);
}
.dish-desc {
  font-size: 24rpx;
  color: var(--text-secondary);
  margin-top: 6rpx;
}
.dish-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8rpx;
}
.dish-price {
  color: var(--price);
  font-weight: 700;
}
.price-symbol {
  font-size: 24rpx;
}
.price-num {
  font-size: 34rpx;
}
.add-btn {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: linear-gradient(180deg, #f5fcf8 0%, #e8f8ee 100%);
  color: #07c160;
  border: 1rpx solid rgba(7, 193, 96, 0.16);
  font-size: 32rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  flex-shrink: 0;
  box-sizing: border-box;
}
.add-btn:active {
  background: linear-gradient(180deg, #f4fcf7 0%, #e3f6ea 100%);
}
.add-btn.disabled {
  background: #f1ece8;
  color: #b2a39d;
  border-color: rgba(189, 175, 169, 0.4);
  font-size: 20rpx;
  width: auto;
  padding: 0 12rpx;
  border-radius: 24rpx;
}
</style>