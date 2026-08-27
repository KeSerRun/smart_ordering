const { request } = require('../utils/request');

function getTableByCode(code) {
  return request({ url: `/table/${code}` });
}

function openTable(id) {
  return request({ url: `/table/${id}/open`, method: 'PUT' });
}

function bindCurrentUser(id) {
  return request({ url: `/table/${id}/bind`, method: 'PUT' });
}

function changeTable(id, targetTableId) {
  return request({ url: `/table/${id}/change`, method: 'PUT', data: { targetTableId } });
}

module.exports = {
  getTableByCode,
  openTable,
  bindCurrentUser,
  changeTable
};
