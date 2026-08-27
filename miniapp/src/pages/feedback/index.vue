<template>
  <view class="feedback-page">
    <app-navbar title="意见反馈" />
    <view class="composer-card">
      <view class="section-title">告诉我们哪里还不够好用</view>
      <view class="section-desc">可以反馈页面问题、点餐流程问题，或你希望增加的功能。</view>

      <textarea
        class="feedback-textarea"
        placeholder="请输入你的意见或建议"
        maxlength="500"
        :value="content"
        @input="onContentInput"
      />

      <input
        class="contact-input"
        placeholder="留下手机号，方便我们联系你"
        maxlength="30"
        :value="contactPhone"
        @input="onPhoneInput"
      />

      <button class="btn-primary submit-btn" :loading="submitting" @tap="handleSubmit">提交反馈</button>
    </view>

    <view class="history-card">
      <view class="history-head">
        <view>
          <view class="section-title">我的反馈</view>
          <view class="section-desc">共 {{total}} 条，回复后会展示在这里</view>
        </view>
      </view>

      <view class="empty" v-if="!loading && list.length === 0">
        <view class="empty-title">还没有反馈记录</view>
        <view class="empty-desc">提交后我们会尽快处理并回复</view>
      </view>

      <view class="feedback-list" v-else>
        <view class="feedback-item" v-for="item in list" :key="item.id">
          <view class="item-head">
            <view class="item-status" :class="item.status === 1 ? 'is-done' : 'is-pending'">{{item.statusText}}</view>
            <view class="item-time">{{item.createTime}}</view>
          </view>
          <view class="item-content">{{item.content}}</view>
          <view class="item-reply" v-if="item.replyContent">
            <view class="reply-label">门店回复</view>
            <view class="reply-content">{{item.replyContent}}</view>
            <view class="reply-time" v-if="item.replyTime">{{item.replyTime}}</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
// 意见反馈
import { reactive, toRefs } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { submitFeedback, getMyFeedback } from '@/api/feedback';
import { KEYS, get } from '@/utils/storage';

function normalizeList(list) {
  if (!Array.isArray(list)) return [];
  return list.map(item => ({
    ...item,
    statusText: Number(item.status) === 1 ? '已回复' : '待回复'
  }));
}

const state = reactive({
  content: '',
  contactPhone: '',
  loading: false,
  submitting: false,
  pageNum: 1,
  pageSize: 20,
  total: 0,
  list: []
});

const {
  content, contactPhone, loading, submitting, total, list
} = toRefs(state);

onShow(() => {
  const userInfo = get(KEYS.USER_INFO) || {};
  state.contactPhone = userInfo.phone || userInfo.mobile || state.contactPhone || '';
  loadData();
});

function onContentInput(e) {
  state.content = e.detail.value;
}

function onPhoneInput(e) {
  state.contactPhone = e.detail.value.trim();
}

async function loadData() {
  state.loading = true;
  try {
    const result = await getMyFeedback(state.pageNum, state.pageSize);
    state.list = normalizeList(result.list || []);
    state.total = Number(result.total || 0);
  } catch (err) {
    uni.showToast({ title: err.message || '加载反馈失败', icon: 'none' });
  } finally {
    state.loading = false;
  }
}

async function handleSubmit() {
  const content = (state.content || '').trim();
  if (!content) {
    uni.showToast({ title: '请先填写反馈内容', icon: 'none' });
    return;
  }

  state.submitting = true;
  try {
    await submitFeedback({
      content,
      contactPhone: state.contactPhone || ''
    });
    uni.showToast({ title: '反馈已提交', icon: 'none' });
    state.content = '';
    await loadData();
  } catch (err) {
    uni.showToast({ title: err.message || '提交失败', icon: 'none' });
  } finally {
    state.submitting = false;
  }
}
</script>

<style scoped>
.feedback-page {
  min-height: 100vh;
  padding: 24rpx;
  background: linear-gradient(180deg, #f7f0ea 0%, #efe6df 100%);
}

.composer-card,
.history-card {
  padding: 28rpx 24rpx;
  border-radius: 28rpx;
  background: rgba(255, 250, 246, 0.96);
  box-shadow: 0 16rpx 36rpx rgba(86, 34, 26, 0.08);
}

.history-card {
  margin-top: 18rpx;
}

.section-title {
  color: #2f241f;
  font-size: 32rpx;
  font-weight: 800;
}

.section-desc {
  margin-top: 10rpx;
  color: #8f7f73;
  font-size: 22rpx;
  line-height: 1.6;
}

.feedback-textarea {
  width: 100%;
  min-height: 240rpx;
  margin-top: 24rpx;
  padding: 22rpx;
  box-sizing: border-box;
  border-radius: 24rpx;
  background: #fff;
  color: #2f241f;
  font-size: 28rpx;
}

.contact-input {
  height: 88rpx;
  margin-top: 18rpx;
  padding: 0 22rpx;
  border-radius: 999rpx;
  background: #fff;
  color: #2f241f;
  font-size: 28rpx;
}

.submit-btn {
  margin-top: 22rpx;
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%) !important;
  color: #ffffff !important;
  border: 1rpx solid rgba(7, 193, 96, 0.28) !important;
  box-shadow: 0 12rpx 26rpx rgba(7, 193, 96, 0.18) !important;
}

.submit-btn[disabled] {
  background: #f1ece8 !important;
  color: #b2a39d !important;
  border-color: rgba(189, 175, 169, 0.4) !important;
  box-shadow: none !important;
}

.history-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.empty {
  padding: 80rpx 0 40rpx;
  text-align: center;
}

.empty-title {
  color: #2f241f;
  font-size: 30rpx;
  font-weight: 700;
}

.empty-desc {
  margin-top: 10rpx;
  color: #8f7f73;
  font-size: 24rpx;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 20rpx;
}

.feedback-item {
  padding: 22rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #fff9f6 0%, #fff 100%);
  border: 1rpx solid #eaded7;
}

.item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
}

.item-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 108rpx;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.item-status.is-pending {
  background: #f8ebe7;
  color: #9f3d2e;
}

.item-status.is-done {
  background: #f4e7e6;
  color: #8b4a3f;
}

.item-time {
  color: #a5968a;
  font-size: 22rpx;
}

.item-content {
  margin-top: 16rpx;
  color: #2f241f;
  font-size: 28rpx;
  line-height: 1.7;
}

.item-reply {
  margin-top: 18rpx;
  padding: 18rpx 18rpx 16rpx;
  border-radius: 20rpx;
  background: #fbefeb;
}

.reply-label {
  color: #9f3d2e;
  font-size: 22rpx;
  font-weight: 700;
}

.reply-content {
  margin-top: 10rpx;
  color: #6e4331;
  font-size: 26rpx;
  line-height: 1.7;
}

.reply-time {
  margin-top: 10rpx;
  color: #a08779;
  font-size: 22rpx;
}
</style>