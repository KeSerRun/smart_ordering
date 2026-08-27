const { request } = require('../utils/request');

function getCart(tableId) {
  return request({ url: '/cart', params: { tableId } });
}

function addCartItem(tableId, dishId, quantity = 1, remark = '') {
  return request({
    url: '/cart/item',
    method: 'POST',
    params: { tableId },
    data: { dishId, quantity, remark }
  });
}

function updateCartItem(dishId, tableId, quantity, remark) {
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

function removeCartItem(dishId, tableId) {
  return request({
    url: `/cart/item/${dishId}`,
    method: 'DELETE',
    params: { tableId }
  });
}

function clearCart(tableId) {
  return request({ url: '/cart', method: 'DELETE', params: { tableId } });
}

module.exports = {
  getCart,
  addCartItem,
  updateCartItem,
  removeCartItem,
  clearCart
};
