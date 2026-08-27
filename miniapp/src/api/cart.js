// 购物车（App 端）
import { request } from '@/utils/request';

export function getCart(tableId) {
  return request({ url: '/cart', params: { tableId } });
}

export function addCartItem(tableId, dishId, quantity = 1, remark = '') {
  return request({
    url: '/cart/item',
    method: 'POST',
    params: { tableId },
    data: { dishId, quantity, remark }
  });
}

export function updateCartItem(dishId, tableId, quantity, remark) {
  const query = [`tableId=${encodeURIComponent(tableId)}`];
  if (quantity !== undefined && quantity !== null) {
    query.push(`quantity=${encodeURIComponent(quantity)}`);
  }
  if (remark !== undefined && remark !== null) {
    query.push(`remark=${encodeURIComponent(remark)}`);
  }
  return request({
    url: `/cart/item/${dishId}?${query.join('&')}`,
    method: 'PUT'
  });
}

export function removeCartItem(dishId, tableId) {
  return request({
    url: `/cart/item/${dishId}`,
    method: 'DELETE',
    params: { tableId }
  });
}

export function clearCart(tableId) {
  return request({ url: '/cart', method: 'DELETE', params: { tableId } });
}