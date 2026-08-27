// 订单（App 端）
import { request } from '@/utils/request';

export function createOrder(payload) {
  return request({ url: '/order', method: 'POST', data: payload });
}

export function addOrderItem(id, payload) {
  return request({ url: `/order/${id}/add-item`, method: 'POST', data: payload });
}

export function rushItem(orderId, itemId) {
  return request({ url: `/order/${orderId}/rush/${itemId}`, method: 'POST' });
}

export function getOrder(id) {
  return request({ url: `/order/${id}` });
}

export function getTableOrders(tableId) {
  return request({ url: `/order/table/${tableId}` });
}