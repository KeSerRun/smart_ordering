const { request } = require('../utils/request');

function getMemberCenter() {
  return request({ url: '/member/me', method: 'GET' });
}

function getMemberLevels() {
  return request({ url: '/member/level/list', method: 'GET' });
}

function getMemberPointsRecords(params) {
  return request({ url: '/member/points-record/page', method: 'GET', params });
}

function getMemberGrowthRecords(params) {
  return request({ url: '/member/growth-record/page', method: 'GET', params });
}

function getMemberRewardSummary(orderId) {
  return request({ url: '/member/reward-summary', method: 'GET', params: { orderId } });
}

function getMemberBenefitOverview() {
  return request({ url: '/member/benefit-overview', method: 'GET' });
}

function exchangeCoupon(exchangeId) {
  return request({ url: `/member/exchange/${exchangeId}`, method: 'POST' });
}

function claimExclusiveCoupon() {
  return request({ url: '/member/exclusive-coupon/claim', method: 'POST' });
}

module.exports = {
  getMemberCenter,
  getMemberLevels,
  getMemberPointsRecords,
  getMemberGrowthRecords,
  getMemberRewardSummary,
  getMemberBenefitOverview,
  exchangeCoupon,
  claimExclusiveCoupon
};
