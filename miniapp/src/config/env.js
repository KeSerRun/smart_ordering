// ========== 环境配置：API 地址 / 平台识别 ==========
const DEVTOOLS_API_HOST = 'http://127.0.0.1:8080';
const REAL_DEVICE_API_HOST = 'https://7a16ed3d.r22.cpolar.top';
const DEV_API_HOST_STORAGE_KEY = 'diancan.devApiHost';

const staticEnv = {
  apiPrefix: '/api/app',
  // 小程序端默认关闭 WebSocket：当前后端是 STOMP 端点，未适配小程序原生 ws 协议
  enableSocket: false,
  // 小程序手机号登录接口
  loginPath: '/api/app/auth/phone-login'
};

function normalizeHost(host) {
  const raw = String(host || '').trim().replace(/\/+$/, '');
  if (!raw) return '';
  if (/^https?:\/\//i.test(raw)) return raw;
  return `http://${raw}`;
}

function getRuntimePlatform() {
  if (typeof uni === 'undefined' || typeof uni.getSystemInfoSync !== 'function') {
    return 'unknown';
  }
  try {
    const info = uni.getSystemInfoSync();
    return info.platform || 'unknown';
  } catch (err) {
    return 'unknown';
  }
}

/**
 * 是否 H5（浏览器）运行环境：uni-app H5 下 uniPlatform 固定为 'web'，
 * 比 info.platform 更可靠（platform 在不同版本可能返回 'h5' 或 'web'）。
 */
function isH5() {
  if (typeof uni === 'undefined' || typeof uni.getSystemInfoSync !== 'function') {
    return false;
  }
  try {
    const info = uni.getSystemInfoSync();
    return info.uniPlatform === 'web' || info.platform === 'web';
  } catch (err) {
    return false;
  }
}

function getStoredApiHost() {
  if (typeof uni === 'undefined' || typeof uni.getStorageSync !== 'function') {
    return '';
  }
  try {
    return normalizeHost(uni.getStorageSync(DEV_API_HOST_STORAGE_KEY));
  } catch (err) {
    return '';
  }
}

function resolveApiHost() {
  const storedApiHost = getStoredApiHost();
  if (storedApiHost) {
    return storedApiHost;
  }
  // H5（浏览器）走 Vite 代理：返回空 host，URL 变成 /api/app/... 交由 /api 代理转发，
  // 同源绕过跨域 CORS。
  if (isH5()) {
    return '';
  }
  // 微信开发者工具（真机联调）用本机地址
  if (getRuntimePlatform() === 'devtools') {
    return DEVTOOLS_API_HOST;
  }
  // 真机联调请改成你电脑的局域网地址，或先通过 storage key 临时覆盖。
  return normalizeHost(REAL_DEVICE_API_HOST) || DEVTOOLS_API_HOST;
}

function resolveWsUrl() {
  return `${resolveApiHost().replace(/^http/i, 'ws')}/ws`;
}

const env = {};

Object.defineProperties(env, {
  apiHost: {
    enumerable: true,
    get: resolveApiHost
  },
  apiPrefix: {
    enumerable: true,
    get: () => staticEnv.apiPrefix
  },
  wsUrl: {
    enumerable: true,
    get: resolveWsUrl
  },
  enableSocket: {
    enumerable: true,
    get: () => staticEnv.enableSocket
  },
  loginPath: {
    enumerable: true,
    get: () => staticEnv.loginPath
  }
});

function getBaseURL(withPrefix = true) {
  return withPrefix ? `${env.apiHost}${env.apiPrefix}` : env.apiHost;
}

export { env, getBaseURL, DEV_API_HOST_STORAGE_KEY };