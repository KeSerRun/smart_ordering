// 会员 / 积分 / 成长值（App 端）
import { request } from '@/utils/request';

export function getMemberCenter() {
  return request({ url: '/member/me', method: 'GET' });
}

export function getMemberLevels() {
  return request({ url: '/member/level/list', method: 'GET' });
}

export function getMemberPointsRecords(params) {
  return request({ url: '/member/points-record/page', method: 'GET', params });
}

export function getMemberGrowthRecords(params) {
  return request({ url: '/member/growth-record/page', method: 'GET', params });
}

export function getMemberRewardSummary(orderId) {
  return request({ url: '/member/reward-summary', method: 'GET', params: { orderId } });
}

export function getMemberBenefitOverview() {
  return request({ url: '/member/benefit-overview', method: 'GET' });
}

export function exchangeCoupon(exchangeId) {
  return request({ url: `/member/exchange/${exchangeId}`, method: 'POST' });
}

export function claimExclusiveCoupon() {
  return request({ url: '/member/exclusive-coupon/claim', method: 'POST' });
}