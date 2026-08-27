const { request } = require('../utils/request');

function createOrder(payload) {
  return request({ url: '/order', method: 'POST', data: payload });
}

function addOrderItem(id, payload) {
  return request({ url: `/order/${id}/add-item`, method: 'POST', data: payload });
}

function rushItem(orderId, itemId) {
  return request({ url: `/order/${orderId}/rush/${itemId}`, method: 'POST' });
}

function getOrder(id) {
  return request({ url: `/order/${id}` });
}

function getTableOrders(tableId) {
  return request({ url: `/order/table/${tableId}` });
}

module.exports = {
  createOrder,
  addOrderItem,
  rushItem,
  getOrder,
  getTableOrders
};
