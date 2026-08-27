<template>
  <view class="container table-page">
    <app-navbar title="扫码开台" />
    <view class="hero-wrap">
      <view class="hero-content">
        <view class="hero-chip">入座开台</view>
        <view class="hero-title">找到你的餐桌</view>
        <view class="hero-sub">关联桌台后，点餐、加菜和支付都会归到当前桌次。</view>
        <view class="hero-table" v-if="table">
          <text class="hero-table-label">当前桌台</text>
          <text class="hero-table-value">{{ (table.code || '-') + ' ' + (table.name || '') }}</text>
        </view>
      </view>
    </view>

    <!-- 未登录时显示手机号登录 -->
    <view class="card login-card" v-if="!loggedIn">
      <view class="section-head">
        <view class="title">微信登录</view>
        <view class="scene-tag">需要登录</view>
      </view>
      <view class="sub">登录后可关联桌台，并把订单同步到当前账号。</view>
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
      <button
        class="btn-primary login-btn"
        :class="{ 'login-btn--disabled': !agreeProtocol }"
        :open-type="agreeProtocol ? 'getPhoneNumber' : ''"
        @tap="handlePhoneLoginTap"
        @getphonenumber="handlePhoneLogin"
      >
        手机号快速登录
      </button>
    </view>

    <view class="card entry-card" v-if="loggedIn">
      <view class="section-head">
        <view class="title">扫码开台</view>
        <view class="scene-tag">桌台关联</view>
      </view>
      <view class="sub">扫桌面小程序码，或输入服务员提供的桌台编码。</view>

      <view class="input-box">
        <view class="input-label">桌台编码</view>
        <input class="input" placeholder="例如 A01" :value="tableCode" @input="onCodeInput" />
      </view>

      <view class="row">
        <button class="btn-primary scan-btn" @tap="scanCode">扫码识别</button>
        <button class="btn-muted query-btn" @tap="loadTable">查询桌台</button>
      </view>
    </view>

    <view class="card table-card" v-if="table">
      <view class="row between">
        <view class="title">当前桌台</view>
        <view class="status" :class="'s' + (table.status === 0 ? 0 : table.status === 1 ? 1 : table.status === 2 ? 2 : 3)">
          {{ table.status === 0 ? '空闲' : table.status === 1 ? '占用' : table.status === 2 ? '已结账' : '待清洁' }}
        </view>
      </view>

      <view class="table-meta">
        <view class="meta-row">
          <text class="label">桌号</text>
          <text class="value">{{ table.code }} / {{ table.name }}</text>
        </view>
        <view class="meta-row">
          <text class="label">区域</text>
          <text class="value">{{ table.areaName || '-' }}</text>
        </view>
      </view>

      <view v-if="entryHintText" class="entry-hint" :class="'entry-hint--' + entryHintTone">
        {{ entryHintText }}
      </view>

      <button class="btn-primary enter-btn" @tap="enterMenu">进入点餐</button>
    </view>
  </view>
</template>

<script setup>
// 桌台页
import { reactive, toRefs } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { KEYS, get } from '@/utils/storage';
import { isLoggedIn, wxLogin, phoneLogin } from '@/utils/auth';
import { bindTableByCode, previewTableByCode, ensureCurrentUserTableBinding, normalizeTableCode } from '@/utils/table-binding';

// ===== 响应式状态 =====
const state = reactive({
  tableCode: '',
  table: null,
  loggedIn: false,
  autoLoadFromSceneDone: false,
  agreeProtocol: false,
  entryHintText: '',
  entryHintTone: ''
});

const {
  tableCode, table, loggedIn, agreeProtocol, entryHintText, entryHintTone
} = toRefs(state);

// ===== 生命周期 =====
onLoad((options) => {
  const code = extractCodeFromOptions(options || {});
  if (!code) return;
  state.tableCode = code;
  state.autoLoadFromSceneDone = false;
});

onShow(() => {
  state.loggedIn = isLoggedIn();

  const cachedTable = get(KEYS.TABLE);
  const sceneTableCode = normalizeTableCode(state.tableCode);
  const cachedTableCode = normalizeTableCode(cachedTable && cachedTable.code);
  if (sceneTableCode && !state.autoLoadFromSceneDone) {
    state.table = null;
    state.autoLoadFromSceneDone = true;
    state.entryHintText = '';
    state.entryHintTone = '';
    loadTable();
    return;
  }

  if (cachedTable) {
    state.table = cachedTable;
    state.tableCode = cachedTable.code || '';
    return;
  }

  if (state.tableCode && !state.autoLoadFromSceneDone) {
    state.autoLoadFromSceneDone = true;
    loadTable();
  }
});

// ===== 方法 =====
function onCodeInput(e) {
  state.tableCode = e.detail.value.trim();
}

/**
 * 手机号登录：open-type="getPhoneNumber" 的回调
 */
async function handlePhoneLogin(e) {
  if (!state.agreeProtocol) {
    uni.showToast({ title: '请先勾选用户协议与隐私政策', icon: 'none' });
    return;
  }
  if (!e.detail.code) {
    uni.showToast({ title: '请授权手机号登录', icon: 'none' });
    return;
  }

  uni.showLoading({ title: '登录中', mask: true });
  try {
    const code = await wxLogin();
    await phoneLogin(code, e.detail.code);
    const cachedTable = get(KEYS.TABLE);
    const reboundTable = await ensureCurrentUserTableBinding(cachedTable);
    state.loggedIn = true;
    state.table = reboundTable || state.table || null;
    state.tableCode = (reboundTable || state.table || {}).code || state.tableCode;
    uni.showToast({ title: '登录成功', icon: 'none' });
  } catch (err) {
    uni.showToast({ title: err.message || '登录失败', icon: 'none' });
  } finally {
    uni.hideLoading();
  }
}

function handlePhoneLoginTap() {
  if (state.agreeProtocol) {
    return;
  }
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

async function scanCode() {
  uni.scanCode({
    onlyFromCamera: true,
    success: async (res) => {
      const content = res.result || '';
      const parsedCode = parseCode(content);
      state.tableCode = parsedCode;
      await loadTable();
    },
    fail: () => {
      uni.showToast({ title: '扫码失败', icon: 'none' });
    }
  });
}

function parseCode(content) {
  if (!content) return '';
  if (content.includes('code=')) {
    const parts = content.split('code=');
    return decodeURIComponent(parts[1].split('&')[0]);
  }
  return content;
}

function extractCodeFromOptions(options) {
  if (options.scene) {
    const decodedScene = decodeURIComponent(options.scene);
    const sceneCode = parseCode(decodedScene);
    if (sceneCode) return sceneCode;
  }
  if (options.code) {
    return parseCode(String(options.code));
  }
  if (options.q) {
    const decodedQ = decodeURIComponent(options.q);
    return parseCode(decodedQ);
  }
  return '';
}

async function loadTable() {
  const tableCode = state.tableCode;
  if (!tableCode) {
    uni.showToast({ title: '请输入桌号编码', icon: 'none' });
    return;
  }
  const requestCode = normalizeTableCode(tableCode);
  state.entryHintText = '';
  state.entryHintTone = '';
  uni.showLoading({ title: '加载中' });
  try {
    const previewTable = await previewTableByCode(tableCode);
    if (requestCode !== normalizeTableCode(state.tableCode)) {
      return;
    }
    const entryHint = buildEntryHint(Number(previewTable.status));
    state.table = previewTable;
    state.tableCode = previewTable.code || tableCode;
    state.entryHintText = entryHint.text;
    state.entryHintTone = entryHint.tone;
  } catch (err) {
    uni.showToast({ title: err.message || '获取桌台失败', icon: 'none' });
  } finally {
    uni.hideLoading();
  }
}

function buildEntryHint(status) {
  if (status === 0) {
    return {
      tone: 'info',
      text: '桌台当前空闲，进入点餐后会自动开台。'
    };
  }

  if (status === 1) {
    return {
      tone: 'warm',
      text: '该桌已有进行中点单，进入后可继续加菜或支付。'
    };
  }

  return {
    tone: '',
    text: ''
  };
}

async function enterMenu() {
  const { table } = state;
  if (!table || !table.id) {
    uni.showToast({ title: '请先关联桌台', icon: 'none' });
    return;
  }
  uni.showLoading({ title: '进入点餐', mask: true });
  try {
    const { table: boundTable } = await bindTableByCode(table.code || state.tableCode);
    state.table = boundTable;
    state.tableCode = boundTable.code || state.tableCode;
    uni.switchTab({ url: '/pages/menu/index' });
  } catch (err) {
    uni.showToast({ title: err.message || '进入点餐失败', icon: 'none' });
  } finally {
    uni.hideLoading();
  }
}
</script>

<style scoped>
.table-page {
  min-height: 100vh;
  padding: 24rpx 22rpx 40rpx;
  box-sizing: border-box;
  background:
    radial-gradient(circle at 92% 4%, rgba(7, 193, 96, 0.10), transparent 26%),
    linear-gradient(180deg, #fbf8f4 0%, #f5efe8 58%, #ece3db 100%);
}

.hero-wrap {
  position: relative;
  margin-bottom: 18rpx;
  border-radius: 24rpx;
  overflow: hidden;
  background:
    linear-gradient(118deg, rgba(255, 255, 255, 0.94) 0%, rgba(255, 249, 239, 0.84) 52%, rgba(226, 247, 232, 0.94) 100%),
    linear-gradient(145deg, #fdf8ef 0%, #eaf8ee 100%);
  border: 1rpx solid rgba(255, 255, 255, 0.82);
  box-shadow: 0 18rpx 42rpx rgba(70, 78, 57, 0.10);
}

.hero-content {
  position: relative;
  z-index: 2;
  padding: 30rpx 28rpx 28rpx;
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 32rpx;
  padding: 0 14rpx;
  border-radius: 999rpx;
  background: rgba(7, 193, 96, 0.10);
  color: #057a3d;
  font-size: 19rpx;
  font-weight: 800;
}

.hero-title {
  margin-top: 14rpx;
  color: #1f2b1d;
  font-size: 42rpx;
  line-height: 1.18;
  font-weight: 900;
}

.hero-sub {
  margin-top: 10rpx;
  max-width: 560rpx;
  color: #788373;
  font-size: 23rpx;
  line-height: 1.55;
}

.hero-table {
  margin-top: 20rpx;
  padding: 16rpx 18rpx;
  border-radius: 18rpx;
  background: rgba(31, 43, 29, 0.90);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  box-shadow: 0 14rpx 28rpx rgba(49, 74, 45, 0.14);
}

.hero-table-label {
  color: rgba(247, 252, 243, 0.66);
  font-size: 21rpx;
}

.hero-table-value {
  color: #f7fcf3;
  font-size: 25rpx;
  font-weight: 800;
  text-align: right;
}

.card {
  margin-top: 18rpx;
  padding: 22rpx;
  border-radius: 24rpx;
  background: rgba(255, 252, 247, 0.92);
  border: 1rpx solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 16rpx 32rpx rgba(67, 72, 57, 0.08);
  box-sizing: border-box;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12rpx;
}

.title {
  color: #1f2b1d;
  font-size: 30rpx;
  line-height: 1.3;
  font-weight: 900;
}

.sub {
  margin-top: 8rpx;
  color: #788373;
  font-size: 23rpx;
  line-height: 1.55;
}

.scene-tag {
  flex-shrink: 0;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #e8f8ee;
  color: #057a3d;
  font-size: 19rpx;
  font-weight: 800;
}

.agreement-row {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-top: 20rpx;
  padding: 16rpx;
  border-radius: 18rpx;
  background: rgba(248, 250, 252, 0.76);
}

.agreement-check {
  width: 30rpx;
  height: 30rpx;
  margin-top: 4rpx;
  border-radius: 999rpx;
  border: 2rpx solid rgba(7, 193, 96, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f7fcf3;
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
  color: #64725f;
  font-size: 22rpx;
  line-height: 1.6;
}

.agreement-link {
  color: #07a857;
  font-weight: 800;
}

.login-btn {
  width: 100%;
  margin-top: 22rpx;
}

.login-btn--disabled {
  background: rgba(255, 255, 255, 0.96) !important;
  color: #07c160 !important;
  border-color: rgba(7, 193, 96, 0.42) !important;
  box-shadow: 0 8rpx 18rpx rgba(7, 193, 96, 0.08) !important;
  opacity: 1;
}

.input-box {
  margin-top: 18rpx;
  padding: 16rpx;
  border-radius: 18rpx;
  background: rgba(248, 250, 252, 0.76);
  border: 1rpx solid rgba(95, 127, 82, 0.08);
}

.input-label {
  color: #64725f;
  font-size: 22rpx;
  font-weight: 800;
}

.input {
  margin-top: 12rpx;
  height: 72rpx;
  padding: 0 20rpx;
  background: #ffffff;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #1f2b1d;
  border: 1rpx solid rgba(95, 127, 82, 0.14);
  box-sizing: border-box;
}

.row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.between {
  justify-content: space-between;
}

.row button {
  flex: 1;
}

.entry-card .row {
  margin-top: 18rpx;
}

.btn-muted {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  margin: 0;
  padding: 0;
  border-radius: 24rpx;
  background: #f8fafc;
  color: #64748b;
  font-size: 28rpx;
  font-weight: 800;
  border: 1rpx solid rgba(100, 116, 139, 0.18);
  box-shadow: 0 8rpx 18rpx rgba(71, 85, 105, 0.05);
}

.scan-btn,
.query-btn {
  min-width: 0;
}

.table-card {
  border-color: rgba(7, 193, 96, 0.12);
}

.table-meta {
  margin-top: 18rpx;
  border-radius: 18rpx;
  background: rgba(248, 250, 252, 0.76);
  padding: 14rpx 16rpx;
}

.entry-hint {
  margin-top: 16rpx;
  padding: 16rpx 18rpx;
  border-radius: 16rpx;
  font-size: 23rpx;
  line-height: 1.6;
}

.entry-hint--success {
  color: #166534;
  background: #edfdf3;
  border: 1rpx solid #b7ebc6;
}

.entry-hint--info {
  color: #1d4f91;
  background: #eef6ff;
  border: 1rpx solid #bfdcff;
}

.entry-hint--warm {
  color: #9a5b18;
  background: #fff5e8;
  border: 1rpx solid #f3d4ab;
}

.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  min-height: 48rpx;
}

.meta-row + .meta-row {
  margin-top: 8rpx;
}

.label {
  color: #8f9a89;
  font-size: 23rpx;
  flex-shrink: 0;
}

.value {
  color: #1f2b1d;
  font-size: 25rpx;
  font-weight: 800;
  text-align: right;
}

.status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 88rpx;
  padding: 5rpx 14rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 800;
}

.status.s0 {
  background: #ecfdf3;
  color: #027a48;
}

.status.s1 {
  background: #f6eee3;
  color: #9a7340;
}

.status.s2 {
  background: #f2f4f7;
  color: #64748b;
}

.status.s3 {
  background: #f7ebe3;
  color: #8b4f2c;
}

.enter-btn {
  width: 100%;
  margin-top: 22rpx;
}
</style>