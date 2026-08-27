const { request } = require('../utils/request');

function submitReview(payload) {
  return request({ url: '/review', method: 'POST', data: payload });
}

function getOrderReview(orderId) {
  return request({ url: `/review/order/${orderId}` });
}

function getMyReviews(pageNum = 1, pageSize = 20) {
  return request({ url: '/review/my', params: { pageNum, pageSize } });
}

module.exports = {
  submitReview,
  getOrderReview,
  getMyReviews
};
