const { env } = require('../config/env');
const { requestRaw } = require('./request');
const { KEYS, get, set } = require('./storage');

/**
 * 调用 wx.login 获取临时凭证 code
 */
function wxLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success: (res) => {
        if (res.code) resolve(res.code);
        else reject(new Error('获取微信登录 code 失败'));
      },
      fail: reject
    });
  });
}

/**
 * 从本地存储恢复登录态
 */
function restoreSession() {
  return {
    token: get(KEYS.TOKEN),
    openid: get(KEYS.OPENID)
  };
}

/**
 * 检查是否有有效 token
 */
function isLoggedIn() {
  return !!get(KEYS.TOKEN);
}

/**
 * 手机号登录：将 code 和 phoneCode 发给后端换取 token
 *
 * @param {string} code       wx.login 返回的临时凭证
 * @param {string} phoneCode  getPhoneNumber 按钮返回的 code
 * @returns {Promise<string>} token
 */
async function phoneLogin(code, phoneCode) {
  const res = await requestRaw({
    url: env.loginPath,
    method: 'POST',
    data: { code, phoneCode },
    withPrefix: false
  });
  const data = res.data || {};
  if (data.token) {
    set(KEYS.TOKEN, data.token);
  }
  return data.token;
}

module.exports = {
  wxLogin,
  restoreSession,
  isLoggedIn,
  phoneLogin
};
