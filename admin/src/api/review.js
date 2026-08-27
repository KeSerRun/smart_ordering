import request from './request'

export const listReviews = (params) => request.get('/admin/review/list', { params })