// 桌台（App 端）
import { request } from '@/utils/request';

export function getTableByCode(code) {
  return request({ url: `/table/${code}` });
}

export function openTable(id) {
  return request({ url: `/table/${id}/open`, method: 'PUT' });
}

export function bindCurrentUser(id) {
  return request({ url: `/table/${id}/bind`, method: 'PUT' });
}

export function changeTable(id, targetTableId) {
  return request({ url: `/table/${id}/change`, method: 'PUT', data: { targetTableId } });
}

export default { getTableByCode, openTable, bindCurrentUser, changeTable };