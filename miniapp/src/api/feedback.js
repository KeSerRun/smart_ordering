// 意见反馈（App 端）
import { request } from '@/utils/request';

export function submitFeedback(data) {
  return request({ url: '/feedback', method: 'POST', data });
}

export function getMyFeedback(pageNum = 1, pageSize = 20) {
  return request({ url: '/feedback/my', params: { pageNum, pageSize } });
}