// 评价（App 端）
import { request } from '@/utils/request';

export function submitReview(payload) {
  return request({ url: '/review', method: 'POST', data: payload });
}

export function getOrderReview(orderId) {
  return request({ url: `/review/order/${orderId}` });
}

export function getMyReviews(pageNum = 1, pageSize = 20) {
  return request({ url: '/review/my', params: { pageNum, pageSize } });
}