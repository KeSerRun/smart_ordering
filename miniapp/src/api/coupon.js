// 优惠券（App 端）
import { request } from '@/utils/request';

export function getMyCoupons(params) {
  return request({ url: '/coupon/my', method: 'GET', params });
}