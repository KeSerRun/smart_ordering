<template>
  <view class="page">
    <app-navbar title="积分明细" />
    <view class="empty" v-if="!loading && records.length === 0">
      <view class="empty-icon">⭐</view>
      <view class="empty-title">暂无积分记录</view>
      <view class="empty-desc">支付成功后获得的积分会显示在这里</view>
    </view>

    <view class="record-list" v-else>
      <view class="record-card" v-for="item in records" :key="item.id">
        <view class="record-head">
          <view class="record-title">{{ item.bizType || '积分变动' }}</view>
          <view class="record-value" :class="item.changeAmount >= 0 ? 'is-plus' : 'is-minus'">
            {{ item.changeAmount >= 0 ? '+' : '' }}{{ item.changeAmount }}
          </view>
        </view>
        <view class="record-meta">
          <text>余额 {{ item.balanceAfter || 0 }}</text>
          <text>{{ item.createTime || '' }}</text>
        </view>
        <view class="record-remark" v-if="item.remark">{{ item.remark }}</view>
      </view>

      <view class="list-footer" v-if="records.length > 0">
        <view class="list-footer__text" v-if="loadingMore">正在加载更多...</view>
        <view class="list-footer__text" v-else-if="finished">已经到底了</view>
        <view class="list-footer__text" v-else>上拉继续加载</view>
      </view>
    </view>
  </view>
</template>

<script setup>
// 积分明细页
import { reactive, toRefs } from 'vue';
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { getMemberPointsRecords } from '@/api/member';

const state = reactive({
  records: [],
  loading: false,
  loadingMore: false,
  finished: false,
  pageNum: 1,
  pageSize: 20,
  total: 0
});

const { records, loading, loadingMore, finished, pageNum, pageSize, total } = toRefs(state);

function resetAndLoad(withRefresh = false) {
  Object.assign(state, {
    records: [],
    pageNum: 1,
    total: 0,
    finished: false
  });
  loadData(true, withRefresh);
}

async function loadData(reset = false, withRefresh = false) {
  if ((state.loading && reset) || state.loadingMore || state.finished) {
    if (withRefresh) uni.stopPullDownRefresh();
    return;
  }

  const nextPage = reset ? 1 : state.pageNum;
  Object.assign(state, reset ? { loading: true } : { loadingMore: true });
  try {
    const result = await getMemberPointsRecords({
      pageNum: nextPage,
      pageSize: state.pageSize
    });
    const newList = Array.isArray(result.list) ? result.list : [];
    const next = reset ? newList : state.records.concat(newList);
    const total = Number(result.total || 0);
    const finished = next.length >= total || newList.length < state.pageSize;

    Object.assign(state, {
      records: next,
      total,
      pageNum: nextPage + 1,
      finished
    });
  } catch (err) {
    uni.showToast({ title: err.message || '加载积分明细失败', icon: 'none' });
  } finally {
    Object.assign(state, { loading: false, loadingMore: false });
    if (withRefresh) uni.stopPullDownRefresh();
  }
}

onShow(() => {
  resetAndLoad();
});

onPullDownRefresh(() => {
  resetAndLoad(true);
});

onReachBottom(() => {
  loadData(false);
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24rpx;
  background:
    radial-gradient(circle at top right, rgba(7, 193, 96, 0.10), transparent 24%),
    linear-gradient(180deg, #f6fbf7 0%, #edf5ec 100%);
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-card {
  padding: 26rpx 24rpx;
  border-radius: 24rpx;
  background: #fbfffb;
  box-shadow: 0 14rpx 30rpx rgba(35, 67, 42, 0.06);
}

.record-head,
.record-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.record-title {
  font-size: 30rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.record-value {
  font-size: 34rpx;
  font-weight: 700;
}

.record-value.is-plus {
  color: var(--success);
}

.record-value.is-minus {
  color: var(--price);
}

.record-meta {
  margin-top: 14rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.record-remark {
  margin-top: 12rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.list-footer {
  padding: 10rpx 0 24rpx;
}

.list-footer__text {
  text-align: center;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.empty {
  margin-top: 180rpx;
  text-align: center;
}

.empty-icon {
  font-size: 70rpx;
}

.empty-title {
  margin-top: 16rpx;
  font-size: 34rpx;
  font-weight: 700;
}

.empty-desc {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
}
</style>