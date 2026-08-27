const { request } = require('../utils/request');

function submitFeedback(data) {
  return request({ url: '/feedback', method: 'POST', data });
}

function getMyFeedback(pageNum = 1, pageSize = 20) {
  return request({ url: '/feedback/my', params: { pageNum, pageSize } });
}

module.exports = {
  submitFeedback,
  getMyFeedback
};
