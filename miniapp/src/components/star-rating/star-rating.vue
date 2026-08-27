<template>
  <view class="star-rating">
    <view
      v-for="(s, i) in stars"
      :key="i"
      class="star"
      :class="[s.filled ? 'filled' : '', props.readonly ? 'readonly' : '']"
      :style="{ fontSize: props.size + 'rpx' }"
      @tap="onTap(i)"
    >★</view>
  </view>
</template>

<script setup>
// 星级评分
import { computed } from 'vue';

const props = defineProps({
  value: { type: Number, default: 0 },
  max: { type: Number, default: 5 },
  size: { type: Number, default: 36 },
  readonly: { type: Boolean, default: false }
});

const emit = defineEmits(['change']);

const stars = computed(() =>
  Array.from({ length: props.max }, (_, i) => ({ filled: i < props.value }))
);

function onTap(index) {
  if (props.readonly) return;
  emit('change', { value: index + 1 });
}
</script>

<style scoped>
.star-rating {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.star {
  color: #e8e8e8;
  transition: color 0.15s;
}
.star.filled {
  color: #ffba00;
}
.star.readonly {
  pointer-events: none;
}
.star:not(.readonly):active {
  transform: scale(1.2);
}
</style>