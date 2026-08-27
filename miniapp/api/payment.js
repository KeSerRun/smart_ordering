const { request } = require('../utils/request');

function wechatPay(orderId) {
  return request({ url: '/payment/wechat', method: 'POST', data: { orderId } });
}

function getPaymentStatus(id) {
  return request({ url: `/payment/${id}/status` });
}

module.exports = {
  wechatPay,
  getPaymentStatus
};
