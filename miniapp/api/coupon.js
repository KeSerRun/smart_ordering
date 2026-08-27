const { request } = require('../utils/request');

function getMyCoupons(params) {
  return request({ url: '/coupon/my', method: 'GET', params });
}

module.exports = {
  getMyCoupons
};
