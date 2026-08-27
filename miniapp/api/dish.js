const { request } = require('../utils/request');

function getCategoryList() {
  return request({ url: '/dish/category/list' });
}

function getDishList() {
  return request({ url: '/dish/list' });
}

function getDishDetail(id) {
  return request({ url: `/dish/${id}` });
}

function searchDish(keyword) {
  return request({ url: '/dish/search', params: { keyword } });
}

module.exports = {
  getCategoryList,
  getDishList,
  getDishDetail,
  searchDish
};
