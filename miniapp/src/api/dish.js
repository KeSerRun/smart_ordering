// 菜品 / 分类 / 规格（App 端）
import { request } from '@/utils/request';

export function getCategoryList() {
  return request({ url: '/dish/category/list' });
}

export function getDishList() {
  return request({ url: '/dish/list' });
}

export function getDishDetail(id) {
  return request({ url: `/dish/${id}` });
}

export function searchDish(keyword) {
  return request({ url: '/dish/search', params: { keyword } });
}