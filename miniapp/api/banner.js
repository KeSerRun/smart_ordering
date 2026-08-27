const { request } = require('../utils/request');

function getBannerList(scene) {
  return request({ url: '/banner/list', method: 'GET', params: scene ? { scene } : {} });
}

module.exports = {
  getBannerList
};
