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
  if (typeof wx === 'undefined' || typeof wx.getSystemInfoSync !== 'function') {
    return 'unknown';
  }

  try {
    const info = wx.getSystemInfoSync();
    return info.platform || 'unknown';
  } catch (err) {
    return 'unknown';
  }
}

function getStoredApiHost() {
  if (typeof wx === 'undefined' || typeof wx.getStorageSync !== 'function') {
    return '';
  }

  try {
    return normalizeHost(wx.getStorageSync(DEV_API_HOST_STORAGE_KEY));
  } catch (err) {
    return '';
  }
}

function resolveApiHost() {
  const storedApiHost = getStoredApiHost();
  if (storedApiHost) {
    return storedApiHost;
  }

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

module.exports = {
  env,
  getBaseURL,
  DEV_API_HOST_STORAGE_KEY
};
