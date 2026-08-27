// 支付（App 端）
import { request } from '@/utils/request';

export function wechatPay(orderId) {
  return request({ url: '/payment/wechat', method: 'POST', data: { orderId } });
}

export function getPaymentStatus(id) {
  return request({ url: `/payment/${id}/status` });
}