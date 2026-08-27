// 首页轮播
import { request } from '@/utils/request';

export function getBannerList(scene) {
  return request({ url: '/banner/list', method: 'GET', params: scene ? { scene } : {} });
}