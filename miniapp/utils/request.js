const { getBaseURL } = require('../config/env');
const { KEYS, get } = require('./storage');

const REQUEST_TIMEOUT = 10000;
const NETWORK_ERROR_MESSAGE = '接口连接失败，请检查后端服务和小程序服务地址配置';

function serializeQuery(params) {
  if (!params) return '';
  const query = Object.keys(params)
    .filter((key) => params[key] !== undefined && params[key] !== null && params[key] !== '')
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&');
  return query ? `?${query}` : '';
}

function normalizeError(err) {
  if (typeof err === 'string') return new Error(err);
  if (err instanceof Error) {
    if (/timeout|timed out|connection|request:fail/i.test(err.message || '')) {
      return new Error(NETWORK_ERROR_MESSAGE);
    }
    return err;
  }
  if (err && typeof err === 'object') {
    const errMsg = err.errMsg || err.message || '';
    if (errMsg) {
      if (/timeout|timed out|connection|request:fail/i.test(errMsg)) {
        return new Error(NETWORK_ERROR_MESSAGE);
      }
      return new Error(String(errMsg));
    }
  }
  return new Error('请求失败');
}

function requestRaw({ url, method = 'GET', data, params, header = {}, withPrefix = true }) {
  const token = get(KEYS.TOKEN);
  const baseURL = getBaseURL(withPrefix);
  const fullUrl = `${baseURL}${url}${serializeQuery(params)}`;

  return new Promise((resolve, reject) => {
    wx.request({
      url: fullUrl,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: token } : {}),
        ...header
      },
      timeout: REQUEST_TIMEOUT,
      success: (res) => {
        const payload = res.data || {};
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(new Error(payload.message || `HTTP ${res.statusCode}`));
          return;
        }
        if (payload.code !== undefined && payload.code !== 200) {
          reject(new Error(payload.message || '业务处理失败'));
          return;
        }
        resolve(payload);
      },
      fail: (err) => {
        console.error('小程序请求失败', { url: fullUrl, err });
        reject(normalizeError(err));
      }
    });
  });
}

function request(options) {
  return requestRaw(options).then((res) => (res.data !== undefined ? res.data : res));
}

module.exports = {
  request,
  requestRaw
};
